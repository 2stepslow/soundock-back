package dopamine.soundock.dto;

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
@Schema(description = "회원 가입 요청 정보")
public class UserSignupRequest {

    @Schema(description = "사용자 이메일 주소")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "비밀번호 (대문자, 숫자, 특수문자 포함 10자 이상, 공백 불가")
    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])\\S{10,}$",
            message = "비밀번호는 대문자, 숫자, 특수문자를 포함하여 10자 이상이어야 하며 공백을 포함할 수 없습니다."
    )
    private String password;

    @Schema(description = "커뮤니티 활동 닉네임 (한글/영문/숫자 10자 이내")
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]{1,10}$",
            message = "닉네임은 특수문자를 제외하고 10자 이내로 입력해주세요."
    )
    private String nickname;

    @Schema(description = "연락처 (숫자만 11자리")
    @NotBlank(message = "연락처는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^010[0-9]{8}$",
            message = "연락처는 숫자만 8자리 입력하세요."
    )
    private String phoneNumber;
}
