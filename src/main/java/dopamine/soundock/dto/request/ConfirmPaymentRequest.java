package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConfirmPaymentRequest {
    @NotBlank
    private String orderId; // 생성한 주문 id

    @NotBlank
    private String paymentKey; // toss에서 받은 paymentKey;

    @NotNull
    @Positive
    private Integer amount; // 결제 금액
}
