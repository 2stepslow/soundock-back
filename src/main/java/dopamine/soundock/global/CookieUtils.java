package dopamine.soundock.global;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;
import java.util.Optional;

/**
 * 쿠키의 생성, 조회, 삭제 및 객체 직렬화를 도와주는 유틸리티 클래스
 */
@Component
public class CookieUtils {

    private static String cookieDomain;

    @Value("${app.cookie.domain}")
    public void setCookieDomain(String domain) {
        CookieUtils.cookieDomain = domain;
    }

    /**
     * [조회] 브라우저가 보낸 요청(Request)에서 특정 이름의 쿠키를 찾아 가져옴
     * Optional을 사용하여 쿠키가 없을 경우에도 안전하게 처리(Null 방지)
     */
    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies(); // 브라우저가 보낸 모든 쿠키를 배열로 가져옴
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                // 우리가 찾는 이름(name)과 일치하는 쿠키가 있는지 확인
                if (cookie.getName().equals(name)) {
                    // 찾았다면 담아서 반환
                    return Optional.of(cookie);
                }
            }
        }
        // 없다면 빈 상자를 반환
        return Optional.empty();
    }

    /**
     * [생성] 서버에서 브라우저로 전달할 새로운 쿠키를 만들어 응답(Response)에 추가
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, value)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(maxAge);

        // 도메인 값이 있을 때만 명시(로컬, 배포 서버 구분)
        if (cookieDomain != null && !cookieDomain.isBlank() && !cookieDomain.equals("localhost")) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie cookie = cookieBuilder.build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * [삭제] 브라우저에 저장된 특정 쿠키를 지우도록 명령
     * 실제로는 쿠키를 '삭제'하는 것이 아니라, 만료 시간을 0으로 만들어 즉시 사라지게힘
     */
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from(name, "")
                .path("/")
                .maxAge(0)
                .httpOnly(true)
                .secure(true)
                .sameSite("None");

        // 도메인 값이 있을 때만 명시(로컬, 배포 서버 구분)
        if (cookieDomain != null && !cookieDomain.isBlank() && !cookieDomain.equals("localhost")) {
            cookieBuilder.domain(cookieDomain);
        }

        ResponseCookie cookie = cookieBuilder.build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    /**
     * [직렬화] 자바 객체를 브라우저에 저장할 수 있도록 문자열로 변환
     * 객체 -> 바이트 배열 -> Base64 문자열 순서
     */
    public static String serialize(Object object) {
        //SerializationUtils를 사용하여 객체를 바이트로 바꾸고, 이를 안전한 Base64 문자로 인코딩
        return Base64.getUrlEncoder().encodeToString(SerializationUtils.serialize(object));
    }

    /**
     * [역직렬화] 쿠키에 저장된 문자열을 다시 자바 객체로 되돌림
     * serialize의 역순으로 변환
     */
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        // 쿠키 값(Base64 문자열)을 다시 바이트 배열로 디코딩한 뒤, 객체로 읽어들임
        try (ByteArrayInputStream bis = new ByteArrayInputStream(Base64.getUrlDecoder().decode(cookie.getValue()));
             ObjectInputStream ois = new ObjectInputStream(bis)) {
            return cls.cast(ois.readObject()); // 요청한 클래스 타입(T)으로 형변환하여 반환
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalArgumentException("Failed to deserialize object", e);
        }
    }
}
