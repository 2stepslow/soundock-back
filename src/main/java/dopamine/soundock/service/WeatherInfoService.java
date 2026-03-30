package dopamine.soundock.service;

import com.fasterxml.jackson.databind.JsonNode;
import dopamine.soundock.dto.request.WeatherInfoRequest;
import dopamine.soundock.dto.response.WeatherInfoResponse;
import dopamine.soundock.exceptions.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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

        if (!resultCode.equals("00")) {
            throw new CustomException("기상청 호출 중 문제가 발생했습니다.",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }


        // 기상청 응답 JSON depth가 깊어, item[]까지 타고 타고 내려감
        JsonNode itemArray = root.path("response")
                .path("body")
                .path("items")
                .path("item");


        String weatherCategory = "";
        String humidityValue = "";


        // itemArray에서 카테고리 꺼냄
        for (JsonNode item : itemArray) {
            String category = item.path("category").asText();

            // 카테고리 중 습도(REH)에 해당하는 값 꺼냄
            if ("REH".equals(category)) {
                weatherCategory = category;
                humidityValue = item.path("obsrValue").asText();
                break;
            }
        }

        int valueToString = Integer.parseInt(humidityValue);
        String careMsg = makeCareMsg(valueToString);

        return WeatherInfoResponse
                .builder()
                .resultCode(resultCode)
                .category(weatherCategory)
                .obsrValue(valueToString)
                .humidityMsg(careMsg)
                .build();
    }

    private String makeCareMsg(int valueToString) {
        if (valueToString <= 30)
            return "공기가 건조해요! 악기 갈라짐을 막기 위해 케이스 보관을 추천합니다.";
        if (valueToString <= 50)
            return "습도가 낮은 편이에요! 장시간 보관 시 케이스 보관을 권장합니다.";
        if (valueToString <= 65)
            return "연주하기 좋은 습도입니다! 쾌적한 연주 환경이에요.";
        if (valueToString <= 75)
            return "습도가 조금 높아요! 연주 후 악기를 잘 닦아 보관하세요.";
        return "습도가 높아요! 악기 변형을 막기 위해 관리에 유의하세요.";
    }
}