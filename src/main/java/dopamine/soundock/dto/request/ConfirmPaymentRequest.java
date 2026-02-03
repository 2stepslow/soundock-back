package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ConfirmPaymentRequest {
    @Schema(description = "주문 ID", example = "uuid-format...")
    @NotBlank
    private String orderId; // 생성한 주문 id

    @Schema(description = "결제 키 (토스에서 발급)", example = "tviva...")
    @NotBlank
    private String paymentKey; // toss에서 받은 paymentKey;

    @Schema(description = "결제 금액", example = "10000")
    @NotNull
    @Positive
    private Integer amount; // 결제 금액
}
