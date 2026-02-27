package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.global.CookieUtils;
import dopamine.soundock.global.constants.AppConstants;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.CookieManager;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/oauth")
@Validated
@Slf4j
public class OauthController {

    private final CookieUtils cookieUtils;

    /**
     * 구글 연동 페이지 리다이렉트 전 사용자의 이메일 쿠키 저장 API
     */
    @GetMapping("/prepare-link")
    public ResponseEntity<RestResponse<Void>> prepareLink(
            @AuthenticationPrincipal(expression = "username") String email,
            HttpServletResponse response
    ) {
        cookieUtils.addCookie(
                response,
                AppConstants.OAuth2.LINKING_USER_EMAIL_COOKIE_NAME,
                email,
                300 // 5분
                );
        return ResponseEntity.ok(RestResponse.success());
    }
}
