package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.TokenDto;
import dopamine.soundock.dto.request.AdminCommentRequest;
import dopamine.soundock.dto.request.LoginRequest;
import dopamine.soundock.dto.response.CancelRequestResponse;
import dopamine.soundock.dto.response.LoginResponse;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.service.AdminService;
import dopamine.soundock.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adm1n")   // 테스트용 임시로 adm1n으로 설정
public class AdminController {

    private final AdminService adminService;
    private final InquiryService inquiryService;

    @Value("${app.cookie.domain}")
    private String cookieDomain;



    // 로그인
    @PostMapping("/login")
    public ResponseEntity<RestResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        TokenDto tokenDto = adminService.login(loginRequest);

        // Refresh Token을 담은 쿠키 생성
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", tokenDto.getRefreshToken())
                .httpOnly(true) // JS에서 접근 불가 (XSS 방어)
                .secure(true) // HTTPS에서만 전송 (테스트 환경에서는 false)
                .path("/") // 모든 경로에서 쿠키 전송
                .maxAge(AppConstants.Time.REFRESH_TOKEN_VALIDITY_MS / 1000)
                .sameSite("none"); // CSRF 방어

        if (cookieDomain != null && !cookieDomain.isBlank() && !cookieDomain.equals("localhost")) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie cookie = cookieBuilder.build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(RestResponse.success("로그인에 성공했습니다.", new LoginResponse(tokenDto.getAccessToken())));
    }


    // 후원 취소 리스트 요청
    @GetMapping("/donations/cancel-requests")
    public ResponseEntity<RestResponse<List<CancelRequestResponse>>> getCancelRequests() {
        List<CancelRequestResponse> responses = adminService.getCancelRequests();
        return ResponseEntity.ok(RestResponse.success(responses));
    }


    // 후원 취소 승인
    @PostMapping("/donations/cancel-requests/{transactionId}")
    public ResponseEntity<RestResponse<String>> approveCancelDonation(
            @PathVariable String transactionId
    ) {
        adminService.approveCancelDonation(transactionId);
        return ResponseEntity.ok(RestResponse.success("승인이 완료 되었습니다."));
    }

    
    /**
     * 1:1 문의 답변
     */
    @PatchMapping("/inquiries/{inquiryId}")
    public ResponseEntity<RestResponse<Void>> registerAdminComment(
            @PathVariable Integer inquiryId,
            @Valid @RequestBody AdminCommentRequest request
    ) {
        inquiryService.registerAdminComment(inquiryId, request);
        return ResponseEntity.ok(RestResponse.success("답변 등록이 완료되었습니다."));
    }

}
