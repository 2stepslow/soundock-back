package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "비밀번호 확인 요청 정보")
public class CurrentPasswdRequest {

    @Schema(description = "현재 비밀번호")
    @NotBlank(message = "현재 비밀번호를 입력해주세요.")
    private String currentPassword;
}
