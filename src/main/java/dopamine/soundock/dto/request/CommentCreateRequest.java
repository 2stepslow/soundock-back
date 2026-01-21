package dopamine.soundock.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommentCreateRequest {
    @NotBlank(message = "공백을 제외한 1글자 이상 입력해주세요.")
    @Size(max = 200, message = "최대 200자까지 입력 가능합니다.")
    private String content;
}
