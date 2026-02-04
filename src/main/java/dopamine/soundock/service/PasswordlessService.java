package dopamine.soundock.service;

import dopamine.soundock.dto.PasswordlessApiResponse;
import dopamine.soundock.dto.response.PWLRegisterResponse;
import dopamine.soundock.dto.response.PWLResultResponse;
import dopamine.soundock.dto.response.PWLStatusResponse;
import dopamine.soundock.dto.response.PWLTriggerResponse;
import dopamine.soundock.entity.RefreshToken;
import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.RefreshTokenRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordlessService {

    private final RestTemplate restTemplate;
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${pwl.serving.url}")
    private String servingApiUrl;

    /**
     * 패스워드리스 서빙 API 호출에 사용할 공통 메서드
     */
    private <T> PasswordlessApiResponse<T> sendRequest(
            String path,
            HttpMethod method,
            MultiValueMap<String, String> params,
            // 자바는 컴파일이 끝나면 제네릭 정보<...>를 지워버리는 특징이 있다.
            // 하지만 API 응답을 받을 때 PasswordlessApiResponse 안에 응답 dto가 들어있다는 것을 '실행 중'에도 알려줘야 올바르게 데이터가 변환
            // 그래서 익명 클래스({})를 생성하여 제네릭 정보를 강제로 보존하는 기법을 사용
            ParameterizedTypeReference<PasswordlessApiResponse<T>> responseType
    ) {
        // URL 빌드
        String url = UriComponentsBuilder.fromUriString(servingApiUrl)
                .path(path)
                .queryParams(params)
                .build()
                .toUriString();

        try {
            // 서빙 API 호출 및 응답 매핑
            ResponseEntity<PasswordlessApiResponse<T>> response = restTemplate.exchange(
                    url, // 요청을 보낼 주소 (상대방의 API 주소)
                    method, // HTTP 방식
                    null, // RequestEntity (요청 보낼 때 담을 헤더나 바디, 이 경우는 필요없으니 null)
                    responseType
            );
            // JSON에서 자바 객체로 변환된 결과물을 return
            return response.getBody();
        } catch (Exception e) {
            // 통신 실패 시 예외 처리
            log.error("에러 메시지: {}", e.getMessage());
            throw new CustomException("서빙 API 통신 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    /**
     * 패스워드리스 가입 확인 메서드
     */
    public PasswordlessApiResponse<PWLStatusResponse> checkUserStatus(String email) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", email);

        return sendRequest(
                "/api/passwordless/status",
                HttpMethod.GET,
                params,
                new ParameterizedTypeReference<PasswordlessApiResponse<PWLStatusResponse>>() {});
    }

    /**
     * 패스워드리스 등록 메서드
     */
    public PasswordlessApiResponse<PWLRegisterResponse> registerUserPWL(String email) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", email);

        return sendRequest(
                "/api/passwordless/register",
                HttpMethod.POST,
                params,
                new ParameterizedTypeReference<PasswordlessApiResponse<PWLRegisterResponse>>() {});
    }

    /**
     * 패스워드리스 로그인 트리거 메서드
     */
    public PasswordlessApiResponse<PWLTriggerResponse> triggerLogin(String email, String ip) {
        // 우리 사이트에 가입된 유저인지 확인
        if (!userRepository.existsByEmail(email)) {
            throw new CustomException("가입되지 않은 회원입니다.", HttpStatus.BAD_REQUEST);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", email);
        params.add("ip", ip);

        return sendRequest(
                "/api/passwordless/login-trigger",
                HttpMethod.POST,
                params,
                new ParameterizedTypeReference<PasswordlessApiResponse<PWLTriggerResponse>>() {}
        );
    }

    /**
     * 패스워드리스 로그인 요청 결과 확인 메서드
     */
    public PasswordlessApiResponse<PWLResultResponse> checkResult(String email, String sessionId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", email);
        params.add("sessionId", sessionId);

        return sendRequest(
                "/api/passwordless/result",
                HttpMethod.GET,
                params,
                new ParameterizedTypeReference<PasswordlessApiResponse<PWLResultResponse>>() {}
        );
    }


    /**
     * 로그인 요청 결과 확인 및 JWT 발급 메서드 (최종 로그인 처리)
     */
    public PasswordlessApiResponse<PWLResultResponse> finalLoginResult(String email, String sessionId) {
        // 서빙 API에 인증 결과 조회
        PasswordlessApiResponse<PWLResultResponse> response = checkResult(email, sessionId);

        if (response == null || response.getData() == null) {
            log.error("인증 결과 조회에 아무것도 없음");
            throw new CustomException("인증 결과 조회에 실패했습니다.", HttpStatus.BAD_REQUEST);
        }

        // 인증이 "Y"인 경우에만 우리 사이트의 로그인 처리 진행
        if ("Y".equals(response.getData().getAuth())) {
            // 우리 서비스 JWT 토큰 생성
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 유저입니다."));

            String accessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getId(), user.getRole().name());
            String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

            // Refresh Token 을 DB에 추가
            RefreshToken refresh = RefreshToken
                    .builder()
                    .token(refreshToken)
                    .user(user)
                    .expirationAt(LocalDateTime.now().plusSeconds(AppConstants.Time.REFRESH_TOKEN_VALIDITY_MS/1000))
                    .build();

            refreshTokenRepository.save(refresh);

            // PWLResultResponse DTO에 토큰 추가
            response.getData().setAccessToken(accessToken);
            response.getData().setRefreshToken(refreshToken);
        }

        return response;
    }

    /**
     * 패스워드리스 인증 요청 취소
     */
    public PasswordlessApiResponse<Void> cancelAuthentication(String email, String sessionId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", email);
        params.add("sessionId", sessionId);

        return sendRequest(
                "/api/passwordless/cancel",
                HttpMethod.POST,
                params,
                new ParameterizedTypeReference<PasswordlessApiResponse<Void>>() {}
        );
    }

    /**
     * 패스워드리스 탈퇴 메서드
     */
    public PasswordlessApiResponse<Void> userWithdrawal(String email) {
        // 사용자가 패스워드리스에 가입되어있는지 확인
        PasswordlessApiResponse<PWLStatusResponse> response = checkUserStatus(email);
        if (!response.getData().isExist()) {
            throw new CustomException("패스워드리스 서비스에 가입되어있지 않습니다.", HttpStatus.BAD_REQUEST);
        }

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", email);

        return sendRequest(
                "/api/passwordless/withdrawal",
                HttpMethod.POST,
                params,
                new ParameterizedTypeReference<PasswordlessApiResponse<Void>>() {}
        );
    }
}
