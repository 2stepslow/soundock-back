package dopamine.soundock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "확인 완료 후 새로 발급 받은 토큰 정보")
public class RefreshResponse {

    @Schema(description = "Refresh Token 확인 후 새로 발급 받은 Access Token (유효기간 1시간)")
    private String accessToken;
}
