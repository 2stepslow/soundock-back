package dopamine.soundock.controller;

import dopamine.soundock.dto.RestResponse;
import dopamine.soundock.dto.request.WeatherInfoRequest;
import dopamine.soundock.dto.response.WeatherInfoResponse;
import dopamine.soundock.service.WeatherInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/weather")
public class WeatherInfoController {
    private final WeatherInfoService weatherInfoService;

    @GetMapping("/info")
    public ResponseEntity<RestResponse<WeatherInfoResponse>> getWeatherInfo(WeatherInfoRequest weatherInfoRequest) {
        WeatherInfoResponse weatherInfoResponse = weatherInfoService.getWeatherInfo(weatherInfoRequest);
        return ResponseEntity.ok(RestResponse.success(weatherInfoResponse));
    }
}