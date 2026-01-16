package dopamine.soundock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dopamine.soundock.config.X1280Properties;
import dopamine.soundock.dto.PWLRequest;
import dopamine.soundock.dto.PWLTokenResponse;
import dopamine.soundock.entity.RefreshToken;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.AuthPendingException;
import dopamine.soundock.exceptions.AuthRejectedException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.global.AESUtil;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.repository.RefreshTokenRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
// application.properties 파일에서 'x1280.user-mock' 이 false 일 때만 이 클래스를 빈으로 등록
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "x1280.use-mock", havingValue = "false", matchIfMissing = true)
public class X1280ServiceImpl implements X1280Service {

    private final RestClient restClient; // 외부 API 호출을 위한 스프링 도구
    private final X1280Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    private static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24;

    public X1280ServiceImpl(X1280Properties properties, TokenProvider tokenProvider, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.properties = properties;
        // API 기본 설정 (기본 URL 및 공통 헤더 추가)
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("X-Server-Key", properties.getServerKey())
                .build();
        this.tokenProvider = tokenProvider;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;

    }

    // 가입 여부 확인
    @Override
    public String isAp(String email) {
        return restClient.post()
                .uri("/ap/rest/auth/isAp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED) // (Key=Value) FORM 데이터 형식으로 body 전송
                .body("userId=" + email)
                .retrieve()
                .body(String.class); // 외부 API에서 보낸 응답을 문자열(JSON)로 받음
    }


    @Override
    public String joinAp(String email) {
        return restClient.post()
                .uri("/ap/rest/auth/joinAp")
                .body(new PWLRequest(email))
                .retrieve()
                .body(String.class);
    }

    @Override
    public String getToken(String email) {
        return restClient.post()
                .uri("/ap/rest/auth/getTokenForOneTime")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("userId=" + email)
                .retrieve()
                .body(String.class);
    }

    // 토큰을 가져와 복호화한 후 SP를 요청
    @Override
    public String getSp(String email, String clientIp) {
        try {
            log.info("getSp 요청 시작: {}", email);
            // 1. 1회용 토큰 요청(getToken)
            String tokenResponse = getToken(email);
            // 1-1. ObjectMapper를 이용해 단순 문자열 tokenResponse를 계층 구조(JSON) 형태로 변환
            JsonNode root = objectMapper.readTree(tokenResponse);

            // 2. JSON 응답에서 "data"라는 이름의 객체(노드)를 찾음
            JsonNode dataNode = root.get("data");
            // 2-1. 만약 dataNode가 비었거나, "token"이라는 키가 없다면 비정상적인 응답이므로 원본 tokenResponse를 그대로 반환
            if (dataNode == null || !dataNode.has("token")) {
                return tokenResponse;
            }
            // 2-2. "token" 키에 해당하는 값을 꺼내서 문자열로 변환
            String encryptedToken = dataNode.get("token").asText();

            // 3. AESUtil 클래스를 사용하여 암호화된 토큰을 현재 가진 'serverKey' 로 풀어냄(복호화)
            String decryptedToken = AESUtil.decrypt(encryptedToken, properties.getServerKey());

            // 4. 중복되지 않는 고유한 ID(UUID)를 생성하여 이번 인증 시도의 '세션 ID'로 사용
            String sessionId = UUID.randomUUID().toString();
            // 4-1. 8자리 랜덤 문자열을 추가적으로 사용
            String randomValue = UUID.randomUUID().toString().substring(0, 8);

            // 5. 복호화한 토큰과 새로 만든 세션 정보들을 담아서 외부 API의 /getSp 경로로 요청을 보냄
            return restClient.post()
                    .uri("/ap/rest/auth/getSp")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED) // 폼 데이터 형태로 전송
                    .body("userId=" + email + // 사용자 ID
                            "&token=" + decryptedToken + // 복호화한 토큰
                            "&random=" + randomValue + // 추가적으로 생성한 랜덤 8자리 문자열값
                            "&sessionId=" + sessionId + // 세션 ID
                            "&clientip=" + clientIp) // 요청한 클라이언트의 IP 주소
                    .retrieve()
                    .body(String.class); // 최종 결과를 문자열로 받음

        } catch (Exception e) {
            // 통신 중 오류가 나거나 복호화에 실패하는 등 예외 발생 시 에러 문자를 JSON 형태로 만들어 반환
            return "{\"result\":false, \"msg\":\"Internal Error: " + e.getMessage() + "\"}";
        }
    }

    @Override
    public String checkResult(String email) {
        return restClient.post()
                .uri("/ap/rest/auth/result")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("userId=" + email)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String cancel(String email, String sessionId) {
        return restClient.post()
                .uri("/ap/rest/auth/cancel")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("userId=" + email + "&sessionId=" + sessionId)
                .retrieve()
                .body(String.class);
    }

    @Override
    public String withdrawalAp(String email) {
        return restClient.post()
                .uri("/ap/rest/auth/withdrawalAp")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body("userId=" + email)
                .retrieve()
                .body(String.class);
    }

    // 인증 결과가 성공했을 때 (auth = Y) 최종 로그인
    @Override
    public PWLTokenResponse verifyAndGenerateTokens(String email) {
        try {
            String response = checkResult(email);
            JsonNode root = objectMapper.readTree(response);

            // 1. result가 false인 경우 (시스템 대기 상태 포함)
            if (!root.path("result").asBoolean()) {
                String code = root.path("code").asText();
                // 711.6 코드는 모바일 응답 대기 중을 의미함
                if ("711.6".equals(code)) {
                    throw new AuthPendingException("모바일 인증 대기 중입니다.");
                }
                // 그 외의 false는 실제 에러 상황
                throw new RuntimeException(root.path("msg").asText());
            }

            // 2. result가 true인 경우 내부 데이터(auth) 상세 체크
            JsonNode dataNode = root.path("data");
            String authStatus = dataNode.path("auth").asText();

            if ("W".equals(authStatus)) {
                // result는 true이지만 사용자가 아직 버튼을 안 누른 상태
                throw new AuthPendingException("모바일 앱에서 승인 버튼을 눌러주세요.");
            } else if ("N".equals(authStatus)) {
                // 사용자가 명확하게 거절을 누른 상태
                throw new AuthRejectedException("사용자에 의해 인증이 거절되었습니다.");
            } else if (!"Y".equals(authStatus)) {
                // Y가 아닌 알 수 없는 상태
                throw new RuntimeException("비정상적인 인증 상태입니다.");
            }

            // 3. 최종 승인(Y)인 경우에만 토큰 발행 진행
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

            // AccessToken 및 RefreshToken 생성 및 저장 로직 (생략)
            String accessToken = tokenProvider.generateAccessToken(user.getEmail());
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

            return PWLTokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .build();

        } catch (AuthPendingException | AuthRejectedException e) {
            // 정의한 커스텀 예외들을 상위(컨트롤러)로 그대로 던짐
            throw e;
        } catch (Exception e) {
            log.error("최종 토큰 발급 중 예외 발생: {}", e.getMessage());
            throw new RuntimeException("인증 처리 중 오류가 발생했습니다.");
        }
    }
}
