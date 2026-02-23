package dopamine.soundock.dto.request;


import dopamine.soundock.global.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentRequest {

    @NotBlank(message = "내용은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.INQUIRY_CONTENT_PATTERN,
            message = AppConstants.ErrorMessage.INQUIRY_CONTENT_ERROR
    )
    private String adminComment;
}
