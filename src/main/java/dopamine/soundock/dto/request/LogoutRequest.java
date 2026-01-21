package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "로그아웃 요청 정보")
public class LogoutRequest {
    @Schema(
            description = "무효화할 Access Token"
    )
    @NotBlank
    private String accessToken;
}
