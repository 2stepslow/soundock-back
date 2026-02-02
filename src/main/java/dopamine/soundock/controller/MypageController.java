package dopamine.soundock.controller;

import dopamine.soundock.dto.*;
import dopamine.soundock.dto.request.CancelUsedPopRequest;
import dopamine.soundock.dto.request.CurrentPasswdRequest;
import dopamine.soundock.dto.request.UpdateInfoRequest;
import dopamine.soundock.dto.request.UpdatePasswdRequest;
import dopamine.soundock.dto.response.*;
import dopamine.soundock.service.InquiryService;
import dopamine.soundock.service.MypageService;
import dopamine.soundock.service.PopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Mypage", description = "마이페이지 기능 관련 API")
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/api/mypage")
public class MypageController {
    private final MypageService mypageService;
    private final PopService popService;
    private final InquiryService inquiryService;

    /** 내 정보 조회 */
    @Operation(
            summary = "내 정보 조회",
            description = "마이페이지 진입 시 유저 프로필과 유튜브 연동 여부를 반환"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내 정보 조회 성공",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @GetMapping("/me")
    public ResponseEntity<RestResponse<MyInfoResponse>> getMyInfo() {
        MyInfoResponse response = mypageService.getMyInfo();
        return ResponseEntity.ok(RestResponse.success(response));
    }


    /** 유저 정보 수정 */
    @Operation(
            summary = "회원 정보 수정(닉네임, 연락처)",
            description = "현재 로그인한 사용자의 닉네임 또는 연락처를 수정. 수정하고 싶은 항목만 선택적으로 가능."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 정보 수정 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (표현식 위반 또는 빈 값 전송), 변경된 사항 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 유저", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "409", description = "중복 오류 (이미 존재하는 닉네임)", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PatchMapping("/me")
    public ResponseEntity<RestResponse<Void>> updateUserInfo(@Valid @RequestBody UpdateInfoRequest request) {
        mypageService.updateUserInfo(request);
        return ResponseEntity.ok(RestResponse.success("회원 정보 수정이 완료되었습니다."));
    }


    /** 비밀번호 수정 */
    @Operation(
            summary = "비밀번호 수정",
            description = "현재 로그인한 사용자의 비밀번호 수정"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "비밀번호 수정 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (표현식 위반 또는 빈 값 전송), 변경된 사항 없음", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 유저", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PatchMapping("/mepasswd")
    public ResponseEntity<RestResponse<Void>> updateUserPasswd(@Valid @RequestBody UpdatePasswdRequest request) {
        mypageService.updateUserPasswd(request);
        return ResponseEntity.ok(RestResponse.success("비밀번호 수정이 완료되었습니다."));
    }


    /** 현재 비밀번호 검증 */
    @Operation(summary = "비밀번호 확인", description = "수정 페이지 진입 전 현재 비밀번호 일치 여부 검증")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "현재 비밀번호 확인 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "일치하지 않는 비밀번호", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "없는 유저", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    @PostMapping("/password/verify")
    public ResponseEntity<RestResponse<Void>> checkCurrentPassword(@Valid @RequestBody CurrentPasswdRequest request) {
        mypageService.checkCurrentPassword(request);
        return ResponseEntity.ok(RestResponse.success("비밀번호 확인에 성공했습니다."));
    }


    /** 회원 탈퇴 */
    @Operation(
            summary = "회원 탈퇴",
            description = "현재 로그인한 사용자의 계정을 탈퇴 처리(Soft Delete)하고, 사용 중인 토큰을 무효화."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 성공"),
            @ApiResponse(responseCode = "400", description = "이미 탈퇴한 사용자이거나 유효하지 않은 토큰 정보"),
            @ApiResponse(responseCode = "401", description = "토큰 정보가 일치하지 않음 (권한 없음)"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 사용자")
    })
    @DeleteMapping("/me")
    public ResponseEntity<RestResponse<Void>> deleteUser() {
        mypageService.deleteUser();
        return ResponseEntity.ok(RestResponse.success("회원 탈퇴가 완료되었습니다."));
    }

    // 재화 구매 내역 조회
    @GetMapping("/pop-purchase")
    public ResponseEntity<RestResponse<List<PaymentHistoryResponse>>> getPaymentHistory() {
        List<PaymentHistoryResponse> paymentHistoryResponses = popService.getPaymentHistory();
        return ResponseEntity.ok(RestResponse.success(paymentHistoryResponses));
    }

    // 재화 사용 내역 조회
    @GetMapping("/pop-usage")
    public ResponseEntity<RestResponse<List<PopHistoryResponse>>> getPopUsageHistory(){
        List<PopHistoryResponse> popHistoryResponses = popService.getPopUsageHistory();
        return ResponseEntity.ok(RestResponse.success(popHistoryResponses));
    }

    // 재화 사용 취소
    @PostMapping("/pop-usage")
    public ResponseEntity<RestResponse<Void>> cancelUsedPop(
            @Valid @RequestBody CancelUsedPopRequest cancelRequest
    ){
        popService.cancelUsedPop(cancelRequest);
        return ResponseEntity.ok(RestResponse.success("재화 사용 취소가 완료되었습니다."));
    /**
     * 1:1 문의 내역 목록 조회
     */
    @Operation(
            summary = "본인 문의 내역 목록 조회",
            description = "현재 로그인한 유저의 문의 내역을 최신순으로 페이징하여 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (토큰 오류)"),
            @ApiResponse(responseCode = "404", description = "유저 미확인")
    })
    @GetMapping("/inquiry")
    public ResponseEntity<RestResponse<Page<InquirySummaryResponse>>> getMyInquiry(
            @AuthenticationPrincipal(expression = "username") String email,
            // 프론트엔드에서 아무런 값을 보내지 않았을 때를 대비한 기본 설정값
            // 1페이지당 10개, 생성일자 기준 최신순 정렬
            @Parameter(description = "페이징 및 정렬 파라미터 (예: page=0&size=10&sort=createdAt,desc)")
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<InquirySummaryResponse> responses = inquiryService.getMyInquiryList(email, pageable);
        return ResponseEntity.ok(RestResponse.success(responses));
    }

    /**
     * 1:1 문의 내역 상세 조회
     */
    @Operation(
            summary = "문의 내역 상세 조회",
            description = "문의 ID를 통해 특정 문의의 상세 내용(본문, 관리자 답변 등)을 조회"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 문의를 찾을 수 없음")
    })
    @GetMapping("/inquiry/{userInquiryId}")
    public ResponseEntity<RestResponse<InquiryDetailResponse>> getInquiryDetail(
            @PathVariable Integer userInquiryId,
            @AuthenticationPrincipal(expression = "username") String email
    ) {
        InquiryDetailResponse response = inquiryService.getInquiry(userInquiryId, email);
        return ResponseEntity.ok(RestResponse.success(response));
    }
}
