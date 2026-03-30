package dopamine.soundock.dto.request;

import dopamine.soundock.enums.Region;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "날씨(습도) 확인 요청 DTO")
public class WeatherInfoRequest {

    @Schema(description = "지역")
    private Region region;
}