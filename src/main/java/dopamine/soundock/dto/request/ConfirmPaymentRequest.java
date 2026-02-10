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
@Schema(description = "결제 최종 승인 요청 (토스 인증 정보)")
public class ConfirmPaymentRequest {
    @Schema(description = "prepare 단계에서 발급받은 주문 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    @NotBlank
    private String orderId; // 생성한 주문 id

    @Schema(description = "토스 결제창 인증 성공 후 받은 PaymentKey", example = "tviva202402011234567890")
    @NotBlank
    private String paymentKey; // toss에서 받은 paymentKey;

    @Schema(description = "토스 인증 결과로 받은 결제 금액 (변조 확인용)", example = "10000")
    @NotNull
    @Positive
    private Integer amount; // 결제 금액
}
