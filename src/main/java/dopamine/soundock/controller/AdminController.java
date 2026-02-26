package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.TokenDto;
import dopamine.soundock.dto.request.AnnouncementCreateRequest;
import dopamine.soundock.dto.request.LoginRequest;
import dopamine.soundock.dto.response.AnnouncementResponse;
import dopamine.soundock.dto.response.CancelRequestResponse;
import dopamine.soundock.dto.response.LoginResponse;
import dopamine.soundock.enums.AnnounceType;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.service.AdminService;
import dopamine.soundock.service.AnnouncementService;
import dopamine.soundock.dto.request.AdminCommentRequest;
import dopamine.soundock.dto.response.*;
import dopamine.soundock.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/adm1n")   // 테스트용 임시로 adm1n으로 설정
public class AdminController {

    private final AdminService adminService;
    private final AnnouncementService announcementService;
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


    // 공지사항 리스트 조회
    @GetMapping("/announcement")
    public ResponseEntity<RestResponse<Page<AnnouncementResponse>>> getAnnouncements(
            @RequestParam(required = false, defaultValue = "0") Integer page
    ) {
        Page<AnnouncementResponse> responses = announcementService.getAnnouncements(page);
        return ResponseEntity.ok(RestResponse.success(responses));
    }


    // 공지사항 상세조회
    @GetMapping("/announcement/{announceId}")
    public ResponseEntity<RestResponse<AnnouncementResponse>> getAnnouncementDetail(
            @PathVariable(required = true) Integer announceId
    ) {
        AnnouncementResponse response = announcementService.getAnnouncementDetail(announceId);
        return ResponseEntity.ok(RestResponse.success(response));
    }
    /**
     * 1:1 문의 전체 목록 조회
     */
    @GetMapping("/inquiries")
    public ResponseEntity<RestResponse<Page<AdminInquiriesListResponse>>> getAdminInquiries(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AdminInquiriesListResponse> responses = inquiryService.getAdminInquiriesList(pageable);
        return ResponseEntity.ok(RestResponse.success(responses));
    }

    /**
     * 1:1 문의 상세 조회
     */
    @GetMapping("/inquiries/{userInquiryId}")
    public ResponseEntity<RestResponse<AdminInquiryDetailResponse>> getInquiryDetail(
            @PathVariable Integer userInquiryId
    ) {
        AdminInquiryDetailResponse response = inquiryService.getAdminInquiry(userInquiryId);
        return ResponseEntity.ok(RestResponse.success(response));
    }



    // 공지사항 작성
    @PostMapping("/announcement/announce/{announceType}")
    public ResponseEntity<RestResponse<Void>> createAnnouncement(
            @PathVariable AnnounceType announceType,
            @Valid @RequestPart("data") AnnouncementCreateRequest createRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        int announceId = adminService.createAnnouncement(announceType, createRequest, files);
        return ResponseEntity.ok(RestResponse.success("등록이 완료 되었습니다."));
    }


    // 공지사항 수정
    @PatchMapping("/announcement/{announceId}")
    public ResponseEntity<RestResponse<Void>> updateAnnouncement(
            @PathVariable Integer announceId,
            @RequestPart("data") AnnouncementCreateRequest updateRequest,
            @RequestPart(value = "files", required = false) List<MultipartFile> newFiles,
            @RequestParam(value = "deleteIds", required = false) List<Integer> deleteAttachmentIds
    ) {
        adminService.updateAnnouncement(announceId, updateRequest, newFiles, deleteAttachmentIds);
        return ResponseEntity.ok(RestResponse.success("수정이 완료 되었습니다."));
    }

    // 공지사항 삭제
    @DeleteMapping("/announcement/{announceId}")
    public ResponseEntity<RestResponse<Void>> deleteAnnouncement(
            @PathVariable Integer announceId
    ) {
        adminService.deleteAnnouncement(announceId);
        return ResponseEntity.ok(RestResponse.success("삭제 되었습니다."));
    }


    /**
     * 1:1 문의 답변
     */
    @PatchMapping("/inquiries/{userInquiryId}")
    public ResponseEntity<RestResponse<Void>> registerAdminComment(
            @AuthenticationPrincipal(expression = "username") String adminEmail,
            @PathVariable Integer userInquiryId,
            @Valid @RequestBody AdminCommentRequest request
    ) {
        inquiryService.registerAdminComment(adminEmail, userInquiryId, request);
        return ResponseEntity.ok(RestResponse.success("답변 등록이 완료되었습니다."));
    }

    @GetMapping("/settlements")
    public ResponseEntity<RestResponse<List<AdminSettlementResponse>>> getAdminSettlement(){
        List<AdminSettlementResponse> adminSettlementResponses = adminService.getAdminSettlement();
        return ResponseEntity.ok(RestResponse.success(adminSettlementResponses));
    }

    // 정산 승인
    @PostMapping("/settlements/approve/{popHistoryId}")
    public ResponseEntity<RestResponse<String>> approveSettlement(
            @PathVariable Integer popHistoryId
    ) {
        adminService.approveSettlement(popHistoryId);
        return ResponseEntity.ok(RestResponse.success("정산 승인이 완료되었습니다."));
    }

    // 정산 거절
    @PostMapping("/settlements/reject/{popHistoryId}")
    public ResponseEntity<RestResponse<String>> rejectSettlement(
            @PathVariable Integer popHistoryId
    ) {
        adminService.rejectSettlement(popHistoryId);
        return ResponseEntity.ok(RestResponse.success("정산이 거절되었습니다."));
    }

}
