package dopamine.soundock.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // 리액트로 돌아갈 주소 설정 (에러 메시지를 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth-redirect")
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        // 인증을 위해 생성했던 임시 쿠키들 삭제
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
