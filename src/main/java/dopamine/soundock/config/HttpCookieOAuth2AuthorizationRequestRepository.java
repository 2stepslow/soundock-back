package dopamine.soundock.config;


import dopamine.soundock.global.CookieUtils;
import dopamine.soundock.global.constants.AppConstants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

// OAuth2 인증 요청 정보를 브라우저 쿠키에 저장하고 관리하는 클래스
@Component
@RequiredArgsConstructor
@Slf4j
public class HttpCookieOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private final CookieUtils cookieUtils;

    /**
     * [조회] 브라우저 쿠키에 저장된 OAuth2 인증 요청 정보를 다시 읽어옴
     * 구글에서 인증을 마치고 우리 서버로 리다이렉트 되었을 때 호출
     */
    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        return cookieUtils.getCookie(request, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME)
                .map(cookie -> cookieUtils.deserialize(cookie, OAuth2AuthorizationRequest.class))
                .orElse(null);
    }

    /**
     * [저장] 사용자가 '유튜브 연동' 버튼을 누른 직후, 구글로 이동하기 전에
     * 인증 과정에 필요한 임시 정보들을 쿠키에 저장
     */
    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request, HttpServletResponse response) {
        // 만약 저장할 인증 정보가 없다면 (인증 과정이 끝났거나 비정상적인 경우) 관련 쿠키들을 모두 삭제
        if (authorizationRequest == null) {
            cookieUtils.deleteCookie(request, response, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
            cookieUtils.deleteCookie(request, response, AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME);
            cookieUtils.deleteCookie(request, response, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME);
            return;
        }

        // OAuth2 인증 요청 정보를 '문자열'로 직렬화하여 쿠키에 저장
        // 객체를 그대로 넣으면 안 되고, 반드시 serialize를 거쳐야 브라우저가 이해가능 하다
        String serializedRequest = cookieUtils.serialize(authorizationRequest);
        cookieUtils.addCookie(response,
                AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME,
                serializedRequest,
                AppConstants.OAuth2.cookieExpireSeconds);

        log.info("OAuth2 인증 요청 쿠키 저장 완료 (Serialized)");

        // 누구의 계정에 연결할지 식별자(Email) 저장
        // 우선순위: 1순위(URL 파라미터), 2순위(현재 로그인된 세션 정보)
        String targetEmail = request.getParameter("email");

        if (targetEmail == null || targetEmail.isBlank()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                targetEmail = ((UserDetails) authentication.getPrincipal()).getUsername();
            }
        }

        if (targetEmail != null && !targetEmail.isBlank()) {
            cookieUtils.addCookie(response, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME,
                    targetEmail, AppConstants.OAuth2.cookieExpireSeconds);
            log.info("연동 대상 이메일 쿠키 저장: {}", targetEmail);
        }

        // 3. 인증 완료 후 프론트엔드(React)의 어느 페이지로 돌아갈지(redirect_uri)를 쿠키에 저장
        String redirectUriAfterLogin = request.getParameter(AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME);
        if (redirectUriAfterLogin != null && !redirectUriAfterLogin.isBlank()) {
            cookieUtils.addCookie(response, AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME, redirectUriAfterLogin, AppConstants.OAuth2.cookieExpireSeconds);
        }
    }

    /**
     * [삭제 전 조회] 인증 과정이 끝나기 직전, 검증을 위해 쿠키 정보를 읽어옴
     */
    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        return this.loadAuthorizationRequest(request);
    }

    /**
     * [일괄 삭제] 인증 성공 혹은 실패 후, 브라우저에 남은 임시 인증용 쿠키들을 모두 지움
     */
    public void removeAuthorizationRequestCookies(HttpServletRequest request, HttpServletResponse response) {
        cookieUtils.deleteCookie(request, response, AppConstants.OAuth2.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        cookieUtils.deleteCookie(request, response, AppConstants.OAuth2.REDIRECT_URI_PARAM_COOKIE_NAME);
        cookieUtils.deleteCookie(request, response, AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME);
    }
}
