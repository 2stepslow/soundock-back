package dopamine.soundock.dto.request;

import dopamine.soundock.global.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CommentCreateRequest {
    @NotBlank(message = "공백을 제외한 1글자 이상 입력해주세요.")
    @Size(min = 1, max = AppConstants.Validation.COMMENT_MAX_LENGTH, message = AppConstants.ErrorMessage.COMMENT_LENGTH_ERROR)
    private String content;
}
