package dopamine.soundock.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpotlightExtendRequest {

    @Min(value = 1000, message = "최소 1000 pop 이상 입력해주세요.")
    private Integer popAmount;
}
