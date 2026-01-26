package dopamine.soundock.controller;

import dopamine.soundock.dto.request.CancelPaymentRequest;
import dopamine.soundock.dto.request.ConfirmPaymentRequest;
import dopamine.soundock.dto.response.ConfirmPaymentResponse;
import dopamine.soundock.dto.request.PreparePaymentRequest;
import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/v1/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    // 결제 주문 정보 생성
    @PostMapping("/prepare")
    public ResponseEntity<RestResponse<?>> preparePayment(
            @Valid @RequestBody PreparePaymentRequest paymentRequest
    ){
        PreparePaymentRequest result = paymentService.preparePayment(paymentRequest);
        return ResponseEntity.ok(RestResponse.success(result));
    }

    // 결제 승인 요청
    @PostMapping("/confirm")
    public ResponseEntity<RestResponse<?>> confirmPayment(
            @Valid @RequestBody ConfirmPaymentRequest confirmPaymentRequest
    ) {
        ConfirmPaymentResponse confirmPaymentResponse = paymentService.confirmPayment(confirmPaymentRequest);
        return ResponseEntity.ok(RestResponse.success(confirmPaymentResponse));
    }

    @GetMapping("/success")
    public ResponseEntity<RestResponse<?>> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam int amount
    ) {
        logger.info("Payment success received. paymeneKey = {}, orderId = {} , amount = {}",
                paymentKey, orderId, amount);
        return ResponseEntity.ok(RestResponse.success("결제 성공"));
    }

    // PaymentKey를 통한 결제 조회
    @GetMapping("/{paymentKey}")
    public ResponseEntity<RestResponse<?>> getPaymentByKey(
            @PathVariable String paymentKey
    ){
         ConfirmPaymentResponse getPaymentResponse = paymentService.getPaymentByKey(paymentKey);
         return ResponseEntity.ok(RestResponse.success(getPaymentResponse));
    }

    // OrderId를 통한 결제 조회
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<RestResponse<?>> getPaymentById(
            @PathVariable String orderId
    ){
        ConfirmPaymentResponse getPaymentResponse = paymentService.getPaymentById(orderId);
        return ResponseEntity.ok(RestResponse.success(getPaymentResponse));
    }

    // 결제 취소
    @PostMapping("/{paymentKey}/cancel")
    public ResponseEntity<RestResponse<?>> cancelPayment(
            @PathVariable String paymentKey,
            @RequestBody CancelPaymentRequest cancelPaymentRequest
    ){
        ConfirmPaymentResponse cancelResponse = paymentService.cancelPayment(paymentKey, cancelPaymentRequest);
        return ResponseEntity.ok(RestResponse.success(cancelResponse));
    }
}
