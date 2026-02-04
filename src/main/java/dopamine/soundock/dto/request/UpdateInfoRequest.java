package dopamine.soundock.dto.request;

import dopamine.soundock.global.constants.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원 정보 수정 요청 정보")
public class UpdateInfoRequest {

    @Schema(description = "변경할 커뮤니티 활동 닉네임 (한글/영문/숫자 10자 이내), 값이 없을 경우 아예 빼고 보내줘야함")
    @Pattern(
            regexp = AppConstants.ValidationPattern.NICKNAME_PATTERN,
            message = AppConstants.ErrorMessage.NICKNAME_FORMAT_ERROR
    )
    private String nickname;

    @Schema(description = "변경할 연락처 (숫자만 11자리), 값이 없을 경우 아예 빼고 보내줘야함")
    @Pattern(
            regexp = AppConstants.ValidationPattern.PHONE_PATTERN,
            message = AppConstants.ErrorMessage.PHONE_FORMAT_ERROR
    )
    private String phoneNumber;
}
