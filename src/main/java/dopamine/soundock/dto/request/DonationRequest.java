package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationRequest {
    // 후원 취소 요청할 때 사용
    @Schema(description = "취소할 후원 내역의 ID (후원 취소 요청 시에만 필수)", example = "152")
    private Integer popHistoryId;

    @Schema(description = "후원할 재화(POP) 금액 (최소 1000 POP)", example = "5000", minimum = "1000")
    @Min(value =1000, message = "후원은 최소 1000 pop 이상 가능합니다.")
    private Integer changeAmount;

    @Schema(description = "후원과 함께 보낼 메시지 (최대 100자)", example = "인재가 여기 있었네")
    @Size(max = 100, message = "후원 메시지를 100자 이내로 작성해주세요.")
    private String message;

}
