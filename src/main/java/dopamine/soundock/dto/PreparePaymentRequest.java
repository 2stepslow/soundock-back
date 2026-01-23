package dopamine.soundock.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PreparePaymentRequest {
    private String orderId; // 생성한 주문 id
    private int changeAmount; // 우리 DB 저장하는 재화 수량
    private int amount; // 실제 결제 금액
}
