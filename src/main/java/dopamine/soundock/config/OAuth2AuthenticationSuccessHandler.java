package dopamine.soundock.config;

import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.service.YouTubeAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 구글로부터 발급된 토큰 세트 가져오기
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = (client.getRefreshToken() != null) ? client.getRefreshToken().getTokenValue() : null;
        Instant expiresAtInstant = client.getAccessToken().getExpiresAt();
        LocalDateTime expiresAt = (expiresAtInstant != null)
                ? LocalDateTime.ofInstant(expiresAtInstant, ZoneId.systemDefault())
                : LocalDateTime.now().plusSeconds(3600); // 만료 정보가 없다면 기본 1시간 설정

        // 2. 유튜브 토큰 정보 DB 저장 (유저 식별 정보와 함께 전달)
        String email = oAuth2User.getAttribute("email");
        youTubeAuthService.saveOrUpdateGoogleTokens(email, accessToken, refreshToken, expiresAt);

        // 3. 우리 서비스 전용 JWT 생성 (기존 로그인 로직 활용)
        String jwtToken = tokenProvider.generateAccessToken(email);

        // 4. 프론트엔드(React)로 리다이렉트 (JWT를 쿼리 파라미터로 전달)
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:3000/oauth-redirect")
                .queryParam("token", jwtToken) // 실제 jwtToken 변수 대입
                .build().toUriString();

        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
