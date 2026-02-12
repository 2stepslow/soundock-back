package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CancelUsedPopRequest {
    @NotNull
    private Integer userId;
    @NotNull
    private Integer popHistoryId;
    @NotNull
    private Integer boardId;
    @Size(min= 10, message = "취소 사유를 10자 이상 입력해주세요.")
    private String cancelReason;
}
