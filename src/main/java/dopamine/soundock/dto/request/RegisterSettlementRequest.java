package dopamine.soundock.dto.request;

import dopamine.soundock.global.constants.AppConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterSettlementRequest {
    @NotNull
    private Integer userId;
    @NotBlank
    private String username;
    @NotBlank(message = "연락처는 필수 입력 항목입니다.")
    @Pattern(
            regexp = AppConstants.ValidationPattern.PHONE_PATTERN,
            message = AppConstants.ErrorMessage.PHONE_FORMAT_ERROR
    )
    private String phoneNumber;
    @NotBlank
    @Pattern(
            regexp = AppConstants.ValidationPattern.ACCOUNT_PATTERN,
            message = AppConstants.ErrorMessage.ACCOUNT_FORMAT_ERROR
    )
    private String accountNumber;

}
