package dopamine.soundock.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "로그인 토큰 요청 정보")
public class RefreshRequest {

    @Schema(description = "로그인 중인 유저의 Refresh Token")
    @NotBlank
    private String refreshToken;
}
