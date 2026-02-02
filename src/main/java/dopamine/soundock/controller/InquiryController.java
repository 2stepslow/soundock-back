package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.InquiryCreateRequest;
import dopamine.soundock.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiry")
@Slf4j
@Tag(name = "Inquiry", description = "1:1 문의 API")
public class InquiryController {

    private final InquiryService inquiryService;

    /**
     * 1:1 문의 등록
     */
    @Operation(
            summary = "1:1 문의 등록",
            description = "사용자가 1:1 문의를 등록합니다. 파일 첨부는 선택사항입니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "문의 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (유효성 검증 실패, 파일 용량 초과 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
            @ApiResponse(responseCode = "500", description = "서버 오류 (파일 업로드 실패 등)")
    })
    @PostMapping("/create")
    public ResponseEntity<RestResponse<String>> createInquiry(
            // @RequestBody는 파일 업로드가 안되서 @ModelAttribute 사용
            @Valid @ModelAttribute InquiryCreateRequest request,
            @AuthenticationPrincipal(expression = "username") String email
            ) {
        inquiryService.registerInquiry(request, email);
        return ResponseEntity.ok(RestResponse.success("문의가 정상적으로 등록되었습니다."));
    }
}
