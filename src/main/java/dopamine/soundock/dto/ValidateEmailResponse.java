package dopamine.soundock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "이메일 사용 가능 여부 응답")
public class ValidateEmailResponse {

    @Schema(description = "이메일 사용 가능 여부")
    private boolean available;

    @Schema(description = "응답 메시지")
    private String message;
}
