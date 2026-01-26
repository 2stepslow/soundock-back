package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "로그인 요청 정보")
public class LoginRequest {
    @Schema(description = "가입된 이메일 주소")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    private String email;

    @Schema(description = "비밀번호")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    private String password;
}
