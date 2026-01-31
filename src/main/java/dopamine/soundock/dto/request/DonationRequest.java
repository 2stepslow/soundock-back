package dopamine.soundock.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationRequest {
    // 후원 취소 요청할 때 사용
    private Integer popHistoryId;

    @NotNull
    private Integer changeAmount;

    @Size(max = 100, message = "후원 메시지를 100자 이내로 작성해주세요.")
    private String message;

}
