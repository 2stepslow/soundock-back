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
public class ResetPasswdRequest {

    @Schema(description = "가입된 사용자의 이메일 주소",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "인증번호 검증 성공 시 발급받은 1회용 토큰(UUID)",
            example = "550e8400-e29b-41d4-a716-446655440000",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "인증 토큰은 필수입니다.")
    private String resetToken;

    @Schema(description = "새로 설정할 비밀번호 (대문자, 숫자, 특수문자 포함 10자 이상)",
            example = "Soundock123!",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "새 비밀번호는 필수입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.PASSWORD_PATTERN,
            message = AppConstants.ErrorMessage.PASSWORD_FORMAT_ERROR
    )
    private String password;
}
