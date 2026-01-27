package dopamine.soundock.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

/**
 * OAuth2 인증 과정에서 오류가 발생했을 때(로그인 취소, 권한 거부 등)
 * 실행되는 로직을 정의하는 핸들러 클래스
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
    // 쿠키 저장소(실패했을 때도 쿠키를 지워야 하므로 가져옴)
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    /**
     * 인증 실패 시 호출되는 핵심 메서드
     * 인증 과정에서 발생한 구체적인 에러 정보가 담겨 있다
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        // 1. 사용자를 다시 보낼 리액트(프론트엔드) 주소를 설정
        // 결과가 '실패'라는 것과, 어떤 에러인지(?error=...) 주소창에 적어서 보냄
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:5173/oauth-redirect")
                .queryParam("error", exception.getLocalizedMessage())
                .build().toUriString();

        // 2. [뒷정리] 인증을 시작할 때 브라우저에 붙여두었던 인증 요청 쿠키 등을 모두 삭제
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);

        // 3. 설정한 리액트 주소로 사용자를 리다이렉트
        // 리액트에서는 이 주소의 'error' 파라미터를 읽어서 "로그인에 실패했습니다" 같은 알림을 띄울 수 있다.
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
