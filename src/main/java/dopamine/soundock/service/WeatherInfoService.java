package dopamine.soundock.service;

import com.fasterxml.jackson.databind.JsonNode;
import dopamine.soundock.dto.request.WeatherInfoRequest;
import dopamine.soundock.dto.response.WeatherInfoResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;


import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
public class WeatherInfoService {

    private final RestClient restClient;

    @Value("${weather.service.key}")
    private String weatherServiceKey;

    // 기상청 API 호출하고 적절한 값 받아오자
    public WeatherInfoResponse getWeatherInfo(WeatherInfoRequest weatherInfoRequest) {

        // 현재시간: 'now'
        LocalDateTime now = LocalDateTime.now();

        // 기상청이 요구하는 날짜형식으로 포맷 가공 (ex. 20260318)
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        String formattedDate = now.format(dateFormatter);

        // 기상청이 요구하는 시간형식으로 포맷 가공 (ex. 0600)
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HHmm");
        String formattedTime = now.format(timeFormatter);


        String url = "http://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getUltraSrtNcst";

        URI uri = UriComponentsBuilder.fromUriString(url)
                .queryParam("serviceKey", weatherServiceKey) // 고정값 (환경변수)
                .queryParam("pageNo", 1) // 고정값
                .queryParam("numOfRows", 10) // 고정값
                .queryParam("dataType", "JSON") // 고정값
                .queryParam("base_date", formattedDate) // 날짜 포맷 가공
                .queryParam("base_time", formattedTime) // 시간 포맷 가공
                .queryParam("nx", weatherInfoRequest.getRegion().getNx()) // 요청DTO 지역명에서 x좌표 추출
                .queryParam("ny", weatherInfoRequest.getRegion().getNy()) // 요청DTO 지역명에서 y좌표 추출
                .build()
                .toUri();


        // 기상청 API 호출 및 내려오는 값을 몽땅 root에 받음
        JsonNode root = restClient.get()
                .uri(uri)
                .retrieve()
                .body(JsonNode.class);


        // resultCode(응답메시지 코드) 꺼냄
        String resultCode = root.path("response")
                .path("header")
                .path("resultCode").asText();


        // 기상청 응답 JSON depth가 깊어, item까지 타고 타고 내려감
        JsonNode itemArray = root.path("response")
                .path("body")
                .path("items")
                .path("item");



        // TODO - 여기부터 완전 엉망진창! 자바 좀더 익히고 마무리 예정!
        String category = "category";
        String obsrValue = "obsrValue";

        // item 배열에서 카테고리 꺼냄???
        for (JsonNode item : itemArray) {
            item.path("category").asText(category);

            // 카테고리 중 습도(REH)에 해당하는 값 꺼냄???
            if ("REH".equals(category)) {
                item.path("obsrValue").asText(obsrValue);
            }
        }

        return WeatherInfoResponse
                .builder()
                // TODO - 작성할예정

                .build();
    }
}