package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "비밀번호 수정 요청 정보")
public class UpdatePasswdRequest {

    @Schema(description = "현재 비밀번호")
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;

    @Schema(description = "변경할 비밀번호 (대문자, 숫자, 특수문자 포함 10자 이상, 공백 불가")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])\\S{10,}$",
            message = "비밀번호는 대문자, 숫자, 특수문자를 포함하여 10자 이상이어야 하며 공백을 포함할 수 없습니다."
    )
    private String password;
}
