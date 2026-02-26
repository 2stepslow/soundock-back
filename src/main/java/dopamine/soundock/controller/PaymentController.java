package dopamine.soundock.controller;

import dopamine.soundock.dto.request.CancelPaymentRequest;
import dopamine.soundock.dto.request.ConfirmPaymentRequest;
import dopamine.soundock.dto.response.ConfirmPaymentResponse;
import dopamine.soundock.dto.request.PreparePaymentRequest;
import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
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
@Tag(name = "Payment (결제)", description = "토스 페이먼츠 연동 및 포인트(POP) 충전/환불 API")
public class PaymentController {
    private final PaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Operation(
            summary = "결제 사전 준비 (주문 ID 생성)",
            description = """
                    **[결제 프로세스 1단계]**
                    
                    토스 페이먼츠 창을 띄우기 전, 서버에서 고유한 **주문 ID(UUID)**를 생성하고
                    결제 대기(PENDING) 상태로 DB에 저장합니다.
                    
                    - **amount**: 실제 결제할 금액 (KRW)
                    - **changeAmount**: 충전될 재화(POP) 수량
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "주문 ID 생성 완료 (토스 결제창 호출 시 사용)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (최소 결제 금액 미달 등)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "401", description = "로그인 필요",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 결제 주문 정보 생성
    @PostMapping("/prepare")
    public ResponseEntity<RestResponse<PreparePaymentRequest>> preparePayment(
            @Valid @RequestBody PreparePaymentRequest paymentRequest
    ){
        PreparePaymentRequest result = paymentService.preparePayment(paymentRequest);
        return ResponseEntity.ok(RestResponse.success(result));
    }

    @Operation(
            summary = "결제 승인 요청 (최종 결제)",
            description = """
                    **[결제 프로세스 2단계 - 최종]**
                    
                    프론트엔드에서 토스 결제창 인증 성공 후, 리턴받은 **paymentKey**와 **orderId**를 실어보냅니다.
                    서버는 토스에 최종 승인을 요청하고, 성공 시 유저의 **POP 잔액을 증가**시킵니다.
                    
                    **[주의사항]**
                    - `prepare` 단계에서 기록한 금액과 현재 요청 금액이 다르면 승인을 거절합니다.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결제 승인 및 POP 충전 성공",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "결제 실패 (금액 불일치, 토스 서버 에러, 한도 초과 등)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "유효하지 않은 주문 ID",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 결제 승인 요청
    @PostMapping("/confirm")
    public ResponseEntity<RestResponse<ConfirmPaymentResponse>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest confirmPaymentRequest
    ) {
        ConfirmPaymentResponse confirmPaymentResponse = paymentService.confirmPayment(confirmPaymentRequest);
        return ResponseEntity.ok(RestResponse.success(confirmPaymentResponse));
    }

    @Operation(
            summary = "결제 성공 리다이렉트 (Success Callback)",
            description = "토스 페이먼츠 위젯 연동 시 `successUrl`로 지정되는 엔드포인트입니다. (주로 로그 기록용)"
    )
    @Parameters({
            @Parameter(name = "paymentKey", description = "토스 결제 키", required = true, example = "tgen_..."),
            @Parameter(name = "orderId", description = "주문 ID", required = true, example = "550e8400-e29b..."),
            @Parameter(name = "amount", description = "결제 금액", required = true, example = "10000")
    })
    @GetMapping("/success")
    public ResponseEntity<RestResponse<Void>> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount
    ) {
        logger.info("Payment success received. paymentKey = {}, orderId = {} , amount = {}",
                paymentKey, orderId, amount);
        return ResponseEntity.ok(RestResponse.success("결제 성공"));
    }

    @Operation(
            summary = "결제 내역 단건 조회 (By PaymentKey)",
            description = "토스에서 발급한 `paymentKey`를 사용하여 **승인 완료된** 결제 상세 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "승인되지 않은 결제 (실패했거나 취소된 건 등)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 PaymentKey",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // PaymentKey를 통한 결제 조회
    @GetMapping("/{paymentKey}")
    public ResponseEntity<RestResponse<ConfirmPaymentResponse>> getPaymentByKey(
            @PathVariable String paymentKey
    ){
         ConfirmPaymentResponse getPaymentResponse = paymentService.getPaymentByKey(paymentKey);
         return ResponseEntity.ok(RestResponse.success(getPaymentResponse));
    }

    @Operation(
            summary = "결제 내역 단건 조회 (By OrderId)",
            description = "시스템 내부에서 생성한 `orderId`(UUID)를 사용하여 결제 상세 내역을 조회합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "해당 주문번호 내역 없음",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // OrderId를 통한 결제 조회
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<ConfirmPaymentResponse>> getPaymentById(
            @PathVariable String orderId
    ){
        ConfirmPaymentResponse getPaymentResponse = paymentService.getPaymentById(orderId);
        return ResponseEntity.ok(RestResponse.success(getPaymentResponse));
    }

    @Operation(
            summary = "결제 취소 (전액 환불)",
            description = """
                    특정 결제 건을 취소하고 결제 금액을 환불합니다.
                    
                    **[취소 실패 조건]**
                    1. 이미 취소된 결제인 경우
                    2. **유저가 충전한 POP을 이미 사용하여 잔액이 부족한 경우** (부분 취소 미지원)
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "취소 성공 (POP 차감 및 카드 취소 완료)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "400", description = "취소 불가 (잔액 부족, 이미 취소됨, 사유 미입력)",
                    content = @Content(schema = @Schema(implementation = RestResponse.class))),
            @ApiResponse(responseCode = "404", description = "결제 내역을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = RestResponse.class)))
    })
    // 결제 취소
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<RestResponse<ConfirmPaymentResponse>> cancelPayment(
            @PathVariable String paymentKey,
            @Valid @RequestBody CancelPaymentRequest cancelPaymentRequest
    ){
        ConfirmPaymentResponse cancelResponse = paymentService.cancelPayment(paymentKey, cancelPaymentRequest);
        return ResponseEntity.ok(RestResponse.success(cancelResponse));
    }
}
