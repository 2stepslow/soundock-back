package dopamine.soundock.config;

import dopamine.soundock.entity.User;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.global.CookieUtils;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.UserRepository;
import dopamine.soundock.service.YouTubeAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

/**
 * OAuth2 인증(구글 로그인)이 성공적으로 완료되었을 때 실행되는 클래스
 * 구글이 준 정보로 우리 서비스의 로그인을 처리하고, 유튜브 토큰을 DB에 저장
 */
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    // 쿠키 저장소 (임시 포스트잇을 지우기 위해 필요)
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;
    // 구글이 준 액세스/리프레시 토큰을 꺼내오기 위한 스프링 표준 서비스
    private final OAuth2AuthorizedClientService authorizedClientService;
    // 유튜브 토큰을 DB에 저장하는 서비스
    private final YouTubeAuthService youTubeAuthService;
    // 우리 사이트 전용 통행증(JWT)을 만들어주는 도구
    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;


    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * 인증 성공 시 스프링 시큐리티가 자동으로 호출하는 메서드
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        // 현재 인증된 정보(토큰, 유저 정보 등)를 가져옴 (우리가 만든거 아님)
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        // 1. 구글로부터 발급된 Access Token과 Refresh Token 세트를 가져옵니다. (우리가 만든거 아님)
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                authToken.getAuthorizedClientRegistrationId(),
                authToken.getName()
        );

        // 2. 이 구글 정보를 누구의 계정에 저장할지 결정
        // 먼저, 우리가 인증 시작할 때 '쿠키'에 적어둔 "연동 시도한 유저 이메일"이 있는지 확인
        String targetEmail = CookieUtils.getCookie(request, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME)
                .map(Cookie::getValue)
                .orElse(oAuth2User.getAttribute("email")); // 쿠키 없으면 구글 이메일 사용
        User user = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new CustomException("존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND));
        // JWT 액세스토큰 생성에 사용할 사용자ID + 권한
        Integer userId = user.getId();
        String role = user.getRole().name();

        // 구글이 준 실제 데이터들을 변수에 담는다.
        String accessToken = client.getAccessToken().getTokenValue();
        String refreshToken = (client.getRefreshToken() != null) ? client.getRefreshToken().getTokenValue() : null;
        Instant expiresAtInstant = client.getAccessToken().getExpiresAt();
        // 만료 시간을 계산 (정보가 없으면 기본 1시간으로 설정)
        LocalDateTime expiresAt = (expiresAtInstant != null)
                ? LocalDateTime.ofInstant(expiresAtInstant, ZoneId.systemDefault())
                : LocalDateTime.now().plusSeconds(3600);

        // 3. 유튜브 열쇠 DB 저장
        // targetEmail의 계정에 유튜브를 쓸 수 있는 열쇠들을 저장 또는 업데이트
        youTubeAuthService.saveOrUpdateGoogleTokens(targetEmail, accessToken, refreshToken, expiresAt);

        // 4. 구글 로그인이 끝난 후 우리 사이트 안에서 돌아다닐 때 쓸 우리의 JWT 토큰을 생성
        /* 이미 로그인한 상태에서 유튜브를 연동하기 때문에 우리서버 토큰이 이미 있는데 왜 또 생성해야하는가?
         * 1) 사용자가 구글 인증 후 우리 사이트로 돌아왔을때 우리서버의 액세스토큰이 만료되는 경우
         * 2) 브라우저가 외부 사이트를 거쳐 돌아오는 동안 기존의 인증 상태가 불안정해지는 경우
         * 3) 나중에 회원가입을 통한 로그인 뿐 아니라 sns나 구글 계정 등으로 로그인을 할 수 있게 되는 경우
         */

        String jwtToken = tokenProvider.generateAccessToken(targetEmail, userId, role);

        // 5. 프론트엔드(React)로 성공 페이지 리다이렉트 할 변수 선언
        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl + "/mypage")
                .queryParam("token", jwtToken)
                .queryParam("success", "true")
                .build().toUriString();

        // 6. 임시 쿠키 삭제
        clearAuthenticationAttributes(request, response);

        // 7. 리다이렉트 실행
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * 인증 과정에서 썼던 임시 데이터(쿠키)들을 삭제
     * (protected 사용 이유)
     * 이 메서드는 부모 클래스(원래의 clearAuthenticationAttributes)의 청소 기능을 우리 식으로 변형한 것이니,
     * "우리 클래스 내부와 우리를 상속받은 자식들 사이에서만 안전하게 공유하자"라는 약속
     * Override를 사용하지 않는 이유는 원래의 clearAuthenticationAttributes는 매개변수가 1개고, 커스텀한 지금 메서드는 매개변수가 2개이기 때문
     */
    protected void clearAuthenticationAttributes(HttpServletRequest request, HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        // 쿠키 저장소의 쿠키 삭제 메서드를 호출
        httpCookieOAuth2AuthorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }
}
