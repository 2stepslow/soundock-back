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

    @Schema(description = "기상 종류 (기온, 풍향, 습도 등)")
    // REH (습도)만 필요
    private String category;

    @Schema(description = "습도값 %")
    private int obsrValue;

    @Schema(description = "습도값에 따른 메시지")
    private String humidityMsg;
}
