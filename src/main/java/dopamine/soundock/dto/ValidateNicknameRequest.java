package dopamine.soundock.dto;

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
public class ValidateNicknameRequest {
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]{1,10}$",
            message = "닉네임은 특수문자를 제외하고 10자 이내로 입력해주세요."
    )
    private String nickname;
}
