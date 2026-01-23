package dopamine.soundock.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConfirmPaymentRequest {
    private String orderId; // 생성한 주문 id
    private String paymentKey; // toss에서 받은 paymentKey;
    private int amount; // 결제 금액
}
