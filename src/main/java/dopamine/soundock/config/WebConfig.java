package dopamine.soundock.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 웹 브라우저의 CORS 정책 설정
    // CORS = 브라우저와 서버 사이의 출입 통제 및 허가 정책
    // ex) react(웹 브라우저) -> spring boot(서버)
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/v1/**") // 해당 경로로 들어오는 요청에 대해
                .allowedOrigins("http://localhost:3000") // 리액트 서버(3000 포트)의 접근을 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 허용할 HTTP 메서드들
                .allowedHeaders("*"); // 모든 헤더 허용
    }
}
