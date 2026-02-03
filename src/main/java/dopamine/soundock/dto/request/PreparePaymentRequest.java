package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "결제 준비 요청 DTO")
public class PreparePaymentRequest {
    @Schema(description = "생성된 주문 ID")
    private String orderId; // 생성한 주문 id

    @Positive
    @Min(100)
    private Integer changeAmount; // 우리 DB 저장하는 재화 수량

    @Schema(description = "결제할 금액 (원화)", example = "10000")
    @Positive
    @NotNull
    private Integer amount; // 실제 결제 금액
}
