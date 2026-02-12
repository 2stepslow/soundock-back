package dopamine.soundock.dto.request;

import dopamine.soundock.global.constants.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "닉네임 중복 확인 요청 DTO")
public class ValidateNicknameRequest {
    @Schema(
            description = "확인할 닉네임(한글, 영문, 숫자 가능, 10자 이내)"
    )
    @NotBlank(message = "닉네임은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.NICKNAME_PATTERN,
            message = AppConstants.ErrorMessage.NICKNAME_FORMAT_ERROR
    )
    private String nickname;
}
