package dopamine.soundock.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreparePaymentRequest {
    private String orderId; // 생성한 주문 id

    @Positive
    @Min(100)
    private Integer changeAmount; // 우리 DB 저장하는 재화 수량

    @Positive
    @NotNull
    private Integer amount; // 실제 결제 금액
}
