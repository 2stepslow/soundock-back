package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardCreateRequest {
    @NotEmpty(message = "최소 1글자 이상 입력해주세요.")
    private String title;

    @NotEmpty(message = "최소 1글자 이상 입력해주세요.")
    private String content;

    private String fileUrl;
}
