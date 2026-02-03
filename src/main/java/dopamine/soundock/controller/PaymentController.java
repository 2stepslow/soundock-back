package dopamine.soundock.controller;

import dopamine.soundock.dto.request.CancelPaymentRequest;
import dopamine.soundock.dto.request.ConfirmPaymentRequest;
import dopamine.soundock.dto.response.ConfirmPaymentResponse;
import dopamine.soundock.dto.request.PreparePaymentRequest;
import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/v1/payments")
@RestController
@Tag(name = "Payment API", description = "결제 시스템 API (토스 페이먼츠 연동)")
public class PaymentController {
    private final PaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Operation(
            summary = "결제 사전 준비 (주문 ID 생성)",
            description = "결제 요청 전, 서버에서 고유한 주문 ID(orderId)를 생성하고 DB에 대기 상태로 기록합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 ID 생성 완료", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "사용자 정보 없음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 결제 주문 정보 생성
    @PostMapping("/prepare")
    public ResponseEntity<RestResponse<?>> preparePayment(
            @Valid @RequestBody PreparePaymentRequest paymentRequest
    ){
        PreparePaymentRequest result = paymentService.preparePayment(paymentRequest);
        return ResponseEntity.ok(RestResponse.success(result));
    }

    @Operation(
            summary = "결제 승인 요청 (최종 결제)",
            description = "토스 페이먼츠에서 결제 인증 후 받은 정보로 최종 승인을 요청합니다.<br>" +
                    "승인이 완료되면 유저의 **재화(POP) 잔액이 증가**합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 승인 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "결제 정보 불일치(금액 위변조 등) 또는 토스 승인 거절", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 주문 ID", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 결제 승인 요청
    @PostMapping("/confirm")
    public ResponseEntity<RestResponse<?>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest confirmPaymentRequest
    ) {
        ConfirmPaymentResponse confirmPaymentResponse = paymentService.confirmPayment(confirmPaymentRequest);
        return ResponseEntity.ok(RestResponse.success(confirmPaymentResponse));
    }

    @Operation(
            summary = "결제 성공 콜백 (프론트엔드 연동용)",
            description = "토스 페이먼츠 위젯 등에서 결제 성공 시 리다이렉트되는 경로입니다. (로그 기록용)"
    )
    @GetMapping("/success")
    public ResponseEntity<RestResponse<?>> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount
    ) {
        logger.info("Payment success received. paymentKey = {}, orderId = {} , amount = {}",
                paymentKey, orderId, amount);
        return ResponseEntity.ok(RestResponse.success("결제 성공"));
    }

    @Operation(
            summary = "PaymentKey로 결제 내역 조회",
            description = "결제 고유 키(PaymentKey)를 사용하여 승인된 결제 상세 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "승인되지 않은 결제이거나 오류 발생", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 PaymentKey", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // PaymentKey를 통한 결제 조회
    @GetMapping("/{paymentKey}")
    public ResponseEntity<RestResponse<?>> getPaymentByKey(
            @PathVariable String paymentKey
    ){
         ConfirmPaymentResponse getPaymentResponse = paymentService.getPaymentByKey(paymentKey);
         return ResponseEntity.ok(RestResponse.success(getPaymentResponse));
    }

    @Operation(
            summary = "OrderId로 결제 내역 조회",
            description = "주문 번호(OrderId)를 사용하여 승인된 결제 상세 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 주문번호 내역 없음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // OrderId를 통한 결제 조회
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<?>> getPaymentById(
            @PathVariable String orderId
    ){
        ConfirmPaymentResponse getPaymentResponse = paymentService.getPaymentById(orderId);
        return ResponseEntity.ok(RestResponse.success(getPaymentResponse));
    }

    @Operation(
            summary = "결제 취소 및 환불",
            description = "특정 결제를 취소하고 환불 처리합니다.<br>" +
                    "**중요:** 충전했던 재화(POP)를 이미 사용하여 **잔액이 부족한 경우 취소가 불가능**합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "취소 성공 (POP 차감 및 환불 완료)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가 (이미 취소됨, POP 잔액 부족, 취소 사유 미입력)", content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없음", content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 결제 취소
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<RestResponse<?>> cancelPayment(
            @PathVariable String paymentKey,
            @Valid @RequestBody CancelPaymentRequest cancelPaymentRequest
    ){
        ConfirmPaymentResponse cancelResponse = paymentService.cancelPayment(paymentKey, cancelPaymentRequest);
        return ResponseEntity.ok(RestResponse.success(cancelResponse));
    }
}
