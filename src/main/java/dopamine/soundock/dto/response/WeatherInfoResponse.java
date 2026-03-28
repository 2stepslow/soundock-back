package dopamine.soundock.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Schema(description = "날씨(습도) 응답 최종 DTO")
public class WeatherInfoResponse {

    @Schema(description = "응답코드")
    private String resultCode;

    @Schema(description = "습도값 %")
    private String obsrValue;
}
