package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.ConfirmPaymentRequest;
import dopamine.soundock.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
@RestController
public class PaymentController {
    private final PaymentService paymentService;

    @PostMapping("/confirm")
    public ResponseEntity<RestResponse<?>> confirmPayment(@RequestBody ConfirmPaymentRequest confirmPaymentRequest) {
        paymentService.confirmPayment(confirmPaymentRequest);
        return ResponseEntity.ok(RestResponse.success());
    }

}
