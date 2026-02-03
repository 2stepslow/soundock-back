package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.DonationRequest;
import dopamine.soundock.dto.response.PopHistoryResponse;
import dopamine.soundock.service.DonationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Donation API", description = "후원 시스템 관련 API (후원하기, 취소하기, 내역 조회)")
public class DonationController {
    private final DonationService donationService;

    @Operation(
            summary = "유저 후원하기",
            description = "로그인한 유저가 특정 회원(targetUserId)에게 재화(POP)를 후원<br>" +
                    "본인에게는 후원할 수 없으며, 보유 재화가 부족할 경우 실패"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "후원 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (재화 부족, 본인 후원 시도, 탈퇴한 유저 등)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "후원 대상 유저를 찾을 수 없음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 후원하기
    @PostMapping("/donations/{targetUserId}")
    public ResponseEntity<RestResponse<?>> donate(
            @PathVariable Integer targetUserId,
            @Valid @RequestBody DonationRequest donationRequest
    ) {
        donationService.donate(targetUserId, donationRequest);
        return ResponseEntity.ok(RestResponse.success("후원을 완료했습니다."));
    }

    @Operation(
            summary = "후원 취소 요청",
            description = "로그인한 유저(userId)가 3일 이내에 수행한 후원 내역에 대해 취소를 요청<br>" +
                    "이미 정산이 진행 중이거나, 취소 기간(3일)이 지난 경우 요청이 거부"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "후원 취소 요청 성공 (관리자 승인 대기)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가 (기간 만료, 이미 취소됨, 권한 없음, 정산 진행 중)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 후원 내역을 찾을 수 없음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 후원 취소 요청
    @PostMapping("/{userId}/donations/cancel")
    public ResponseEntity<RestResponse<?>> cancelDonation(
            @PathVariable Integer userId,
            @RequestBody DonationRequest donationCancelRequest
    ){
        donationService.cancelDonation(userId, donationCancelRequest);
        return ResponseEntity.ok(RestResponse.success("후원 취소 요청을 완료했습니다. 관리자 확인 후 취소 요청이 완료됩니다."));
    }

    @Operation(
            summary = "내가 후원한 내역 조회 (Donor)",
            description = "로그인한 유저(userId)가 다른 사람에게 후원한 모든 내역을 최신순으로 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음 (본인 내역만 조회 가능)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "후원 내역이 존재하지 않음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 후원 내역 조회(후원자)
    @GetMapping("/{userId}/donor")
    public ResponseEntity<RestResponse<List<PopHistoryResponse>>> getDonatedResult(
            @PathVariable Integer userId
    ){
        List<PopHistoryResponse> donatedPopResponses = donationService.getDonatedResult(userId);
        return ResponseEntity.ok(RestResponse.success(donatedPopResponses));
    }

    @Operation(
            summary = "내가 받은 후원 내역 조회 (Acceptor)",
            description = "로그인한 유저(userId)가 다른 사람으로부터 받은 후원 내역을 최신순으로 조회"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "403", description = "조회 권한 없음 (본인 내역만 조회 가능)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "후원 받은 내역이 존재하지 않음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 후원 내역 조회(수혜자)
    @GetMapping("/{userId}/acceptor")
    public ResponseEntity<RestResponse<List<PopHistoryResponse>>> getReceivedResult(
            @PathVariable Integer userId
    ){
        List<PopHistoryResponse> receivedPopResponse = donationService.getReceivedResult(userId);
        return ResponseEntity.ok(RestResponse.success(receivedPopResponse));
    }
}
