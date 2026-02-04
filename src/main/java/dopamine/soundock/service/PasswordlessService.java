package dopamine.soundock.service;

import dopamine.soundock.dto.PasswordlessApiResponse;
import dopamine.soundock.dto.response.PWLRegisterResponse;
import dopamine.soundock.dto.response.PWLStatusResponse;
import dopamine.soundock.exceptions.CustomException;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordlessService {

    private final RestTemplate restTemplate;

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

}
