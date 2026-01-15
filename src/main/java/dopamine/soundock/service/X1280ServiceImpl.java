package dopamine.soundock.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dopamine.soundock.config.X1280Properties;
import dopamine.soundock.dto.PWLJoinRequest;
import dopamine.soundock.global.AESUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Slf4j
@Service
// application.properties 파일에서 'x1280.user-mock' 이 false 일 때만 이 클래스를 빈으로 등록
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "x1280.use-mock", havingValue = "false", matchIfMissing = true)
public class X1280ServiceImpl implements X1280Service {

    private final RestClient restClient; // 외부 API 호출을 위한 스프링 도구
    private final X1280Properties properties;
    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON 파싱용

    public X1280ServiceImpl(X1280Properties properties) {
        this.properties = properties;
        // API 기본 설정 (기본 URL 및 공통 헤더 추가)
        this.restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .defaultHeader("X-Server-Key", properties.getServerKey())
                .build();
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
                .body(new PWLJoinRequest(email))
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
}
