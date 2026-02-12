package dopamine.soundock.dto.request;

import dopamine.soundock.global.constants.AppConstants;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "회원 정산 정보 등록")
public class RegisterSettlementRequest {
    @Schema(description = "사용자 이메일 주소")
    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    private String email;

    @Schema(description = "사용자 실명")
    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.NAME_PATTERN,
            message = AppConstants.ErrorMessage.NAME_PATTERN_ERROR
    )
    private String name;

    @Schema(description = "사용자 연락처")
    @NotBlank(message = "연락처는 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.PHONE_PATTERN,
            message = AppConstants.ErrorMessage.PHONE_FORMAT_ERROR
    )
    private String phoneNumber;

    @Schema(description = "사용자 계좌번호")
    @NotBlank(message = "계좌번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.ACCOUNT_PATTERN,
            message = AppConstants.ErrorMessage.ACCOUNT_FORMAT_ERROR
    )
    private String accountNumber;

}
