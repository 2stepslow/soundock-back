package dopamine.soundock.config;


import dopamine.soundock.global.CookieUtils;
import dopamine.soundock.global.constants.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@Component
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return CookieUtils.getCookie(request, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> CookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null) {
            CookieUtils.deleteCookie(request, response, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            CookieUtils.deleteCookie(request, response, AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME);
            CookieUtils.deleteCookie(request, response, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME);
            return;
        }
        CookieUtils.addCookie(response, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                CookieUtils.serialize(authorizationRequest), AppConstants.OAuth2.cookieExpireSeconds);

        // 현재 Spring Security에 로그인된 유저 정보를 가져와 쿠키에 저장
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            String currentEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            CookieUtils.addCookie(response, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME, currentEmail, AppConstants.OAuth2.cookieExpireSeconds);
        }

        String redirectUriAfterLogin = request.getParameter(AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            CookieUtils.addCookie(response, AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, AppConstants.OAuth2.cookieExpireSeconds);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        CookieUtils.deleteCookie(request, response, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME);
        CookieUtils.deleteCookie(request, response, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME);
    }
}
