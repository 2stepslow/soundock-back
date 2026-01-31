package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.DonationRequest;
import dopamine.soundock.dto.response.PopHistoryResponse;
import dopamine.soundock.service.DonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class DonationController {
    private final DonationService donationService;

    // 후원하기
    @PostMapping("/donations/{targetUserId}")
    public ResponseEntity<RestResponse<?>> donate(
            @PathVariable Integer targetUserId,
            @Valid @RequestBody DonationRequest donationRequest
    ) {
        donationService.donate(targetUserId, donationRequest);
        return ResponseEntity.ok(RestResponse.success("후원을 완료했습니다."));
    }

    // 후원 취소 요청
    @PostMapping("/{userId}/donations/{donationId}")
    public ResponseEntity<RestResponse<?>> cancelDonation(
            @PathVariable Integer userId,
            DonationRequest donationCancelRequest
    ){
        donationService.cancelDonation(userId, donationCancelRequest);
        return ResponseEntity.ok(RestResponse.success("후원 취소 요청을 완료했습니다. 관리자 확인 후 취소 요청이 완료됩니다."));
    }

    // 후원 내역 조회(후원자)
    @GetMapping("/{userId}/donor")
    public ResponseEntity<RestResponse<List<PopHistoryResponse>>> getDonatedResult(
            @PathVariable Integer userId
    ){
        List<PopHistoryResponse> donatedPopResponses = donationService.getDonatedResult(userId);
        return ResponseEntity.ok(RestResponse.success(donatedPopResponses));
    }

    // 후원 내역 조회(수혜자)
    @GetMapping("/{userId}/acceptor")
    public ResponseEntity<RestResponse<List<PopHistoryResponse>>> getReceivedResult(
            @PathVariable Integer userId
    ){
        List<PopHistoryResponse> receivedPopResponse = donationService.getReceivedResult(userId);
      return ResponseEntity.ok(RestResponse.success(receivedPopResponse));
    }
}
