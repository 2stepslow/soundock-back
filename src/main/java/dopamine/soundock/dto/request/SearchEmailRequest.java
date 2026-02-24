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
@Schema(description = "이메일 찾기 요청 정보")
public class SearchEmailRequest {

    @Schema(description = "사용자의 실명 (숫자, 특수문자 금지)")
    @NotBlank(message = "사용자 이름은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.REAL_NAME_PATTERN,
            message = AppConstants.ErrorMessage.REAL_NAME_PATTERN_ERROR
    )
    private String name;

    @Schema(description = "연락처 (숫자만 11자리")
    @NotBlank(message = "연락처는 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.PHONE_PATTERN,
            message = AppConstants.ErrorMessage.PHONE_FORMAT_ERROR
    )
    private String phoneNumber;
}
