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
@Schema(description = "결제 사전 준비 요청 (금액 및 재화 정보)")
public class PreparePaymentRequest {
    @Schema(description = "서버에서 생성된 주문 ID (응답 시에만 값 존재, 요청 시에는 null)", accessMode = Schema.AccessMode.READ_ONLY)
    private String orderId; // 생성한 주문 id

    @Schema(description = "충전할 재화(POP) 수량", example = "100")
    @Positive
    @Min(100)
    private Integer changeAmount; // 우리 DB 저장하는 재화 수량

    @Schema(description = "실제 결제할 금액 (KRW)", example = "10000")
    @Positive
    @NotNull
    private Integer amount; // 실제 결제 금액
}
