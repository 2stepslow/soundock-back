package dopamine.soundock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "로그인 성공 응답(토큰 정보)")
public class LoginResponse {
    @Schema(description = "API 호출 시 인증에 필요한 Access Token (유효기간 1시간)")
    private String accessToken;

    @Schema(description = "Access Token 만료 시 재발급을 위한 Refresh Token (유효기간 하루)")
    private String refreshToken;
}
