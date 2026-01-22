package dopamine.soundock.config;

import dopamine.soundock.global.CookieUtils;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.service.YouTubeAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final YouTubeAuthService youTubeAuthService;
    private final TokenProvider tokenProvider;

    // application.properties에서 환경 변수 주입
    @Value("${app.cookie.secure}")
    private boolean secureCookie;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 구글로부터 발급된 토큰 세트 가져오기
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        // 2. 어떤 유저에게 이 토큰을 저장할지 결정
        // 쿠키에서 "연동을 시도했던 우리 서비스 유저 이메일"을 먼저 찾음
        String targetEmail = CookieUtils.getCookie(request, HttpCookieOAuth2AuthorizationRequestRepository.LINKING_USER_EMAIL_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(oAuth2User.getAttribute("email")); // 쿠키 없으면 구글 이메일 사용

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = (client.getRefreshToken() != null) ? client.getRefreshToken().getTokenValue() : null;
        Instant expiresAtInstant = client.getAccessToken().getExpiresAt();
        LocalDateTime expiresAt = (expiresAtInstant != null)
                ? LocalDateTime.ofInstant(expiresAtInstant, ZoneId.systemDefault())
                : LocalDateTime.now().plusSeconds(3600); // 만료 정보가 없다면 기본 1시간 설정

        // 2. 유튜브 토큰 정보 DB 저장 (유저 식별 정보와 함께 전달)
        youTubeAuthService.saveOrUpdateGoogleTokens(targetEmail, accessToken, refreshToken, expiresAt);

        // 3. 우리 서비스 전용 JWT 생성 (기존 로그인 로직 활용)
        String jwtToken = tokenProvider.generateAccessToken(targetEmail);

        // 4. JWT를 HttpOnly 쿠키에 담기
        addJwtCookie(response, jwtToken);

        // 5. 프론트엔드(React)로 리다이렉트
        String targetUrl = frontendUrl + "/oauth-redirect?success=true";

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // JWT 쿠키 추가 헬퍼 메서드
    private void addJwtCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("access_token", token)
                .httpOnly(true)
                .secure(secureCookie)      // 개발 환경에서는 false로 설정 (HTTPS를 안쓰기 때문에)
                .path("/")
                .maxAge(60 * 60)   // 1시간
                .sameSite("Lax")   // Spring Boot 2.6+에서 사용 가능
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
