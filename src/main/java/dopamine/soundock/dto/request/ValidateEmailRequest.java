package dopamine.soundock.dto.request;

import dopamine.soundock.global.constants.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "이메일 중복 확인 요청 DTO")
public class ValidateEmailRequest {

    @Schema(description = "확인할 이메일 주소")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.EMAIL_PATTERN,
            message = AppConstants.ErrorMessage.EMAIL_PATTERN_ERROR
    )
    private String email;
}
