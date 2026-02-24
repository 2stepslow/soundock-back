package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class VerifyCodeRequest {

    @Schema(description = "가입된 사용자의 이메일 주소",
            example = "user@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "이메일로 발송된 6자리 인증번호",
            example = "123456",
            requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "인증번호를 입력해주세요.")
    @Size(min = 6, max = 6, message = "인증번호는 6자리여야 합니다.")
    private String code;
}
