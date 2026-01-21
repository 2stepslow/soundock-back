package dopamine.soundock.config;

import dopamine.soundock.global.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final HttpCookieOAuth2AuthorizationRequestRepository httpCookieOAuth2AuthorizationRequestRepository;

    private final OAuth2AuthenticationSuccessHandler oAuth2AuthenticationSuccessHandler;
    private final OAuth2AuthenticationFailureHandler oAuth2AuthenticationFailureHandler;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "http://localhost:5173",
                "http://192.168.200.84:5173",
                "http://192.168.56.1:5173",
                "http://192.168.200.9:5173",
                "http://192.168.200.45:5173"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS")); // 허용할 HTTP 메서드들
        configuration.addAllowedHeader("*"); // 모든 헤더 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                // Google OAuth2
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(
                                        new CustomAuthorizationRequestResolver(clientRegistrationRepository))
                                .authorizationRequestRepository(httpCookieOAuth2AuthorizationRequestRepository)
                        )
                        // 로그인 성공 시 실행할 핸들러 등록 (DB 저장 로직)
                        .successHandler(oAuth2AuthenticationSuccessHandler)
                        // 로그인 실패 시 핸들러
                        .failureHandler(oAuth2AuthenticationFailureHandler)
                )

                // CORS 설정 (프론트엔드 React와 통신을 위해 필수)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // csrf는 Jwt 토큰으로 인해 어차피 막히기 때문에 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // 서버가 사용자의 상태를 세션에 저장하지 않도록 설정
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v1/payments/**","/payment/**").permitAll() // ** : 테스트용 /api/payments/, /payment/ 뒤의 모든 것들 허용
                        .requestMatchers("/api/auth/**").permitAll() // ** : /api/auth/ 뒤의 모든 것들 허용
                        .requestMatchers("/api/boards/**").permitAll() // ** : 테스트용 /api/boards/ 뒤의 모든 것들 허용
                        .requestMatchers(
                                "/payment/*.html",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**"

                        ).permitAll()
                        .requestMatchers("/api/test/**").permitAll()
                        .anyRequest().authenticated() // 그 외의 요청은 인증 필요
                )
                // 직접만든 jwtAuthenticationFilter를 UsernamePasswordAuthenticationFilter 보다 먼저 실행되도록 설정
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                // 예외 처리 (인증 실패 시 401 에러를 더 명확하게 반환)
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            // 1. 응답 타입을 JSON, 한글(UTF-8)로 설정
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

                            // 2. RestResponse 규격에 맞는 JSON 문자열 직접 생성
                            String jsonResponse = "{" +
                                    "\"success\": false," +
                                    "\"message\": \"로그인이 필요한 서비스입니다.\"," +
                                    "\"data\": null" +
                                    "}";

                            response.getWriter().write(jsonResponse);
                        })
                );
        return http.build();
    }

    // 커스텀 리졸버 클래스
    public static class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {
        private final OAuth2AuthorizationRequestResolver defaultResolver;

        public CustomAuthorizationRequestResolver(ClientRegistrationRepository repo) {
            this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
            OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
            return customize(authRequest);
        }

        @Override
        public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
            OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
            return customize(authRequest);
        }

        private OAuth2AuthorizationRequest customize(OAuth2AuthorizationRequest authRequest) {
            if (authRequest == null) return null;

            // 구글 로그인 시에만 특정 파라미터 추가
            Map<String, Object> extraParams = new HashMap<>(authRequest.getAdditionalParameters());
            extraParams.put("access_type", "offline"); // 리프레시 토큰 발급의 핵심

            // 개발 중일 때는 (true) prompt=consent
            // 배포 환경에서는 (false) prompt=select_account 로 변경
            boolean isDevelopment = true;
            if (isDevelopment) {
                extraParams.put("prompt", "consent");    // 매번 동의 화면을 띄워 리프레시 토큰 재발급 강제
            } else {
                extraParams.put("prompt", "select_account"); // 계정 선택 창만 띄우고, 동의 화면은 최초 1회만 표시
            }


            return OAuth2AuthorizationRequest.from(authRequest)
                    .additionalParameters(extraParams)
                    .build();
        }
    }
}
