package dopamine.soundock.global.constants;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AppConstants {
    /**
     * 시간 관련 상수값
     */
    public static class Time {
        // 엑세스 토큰 유효기간
        public static final long ACCESS_TOKEN_VALIDITY_MS = 1000L * 60 * 60; // 1 hour
        // 리프레시 토큰 유효기간
        public static final long REFRESH_TOKEN_VALIDITY_MS = 1000L * 60 * 60 * 24; // 24 hours
        // 재가입 불가 기간
        public static final int EMAIL_REACTIVATION_COOLDOWN_DAYS = 30; // 30 days
        // 이메일 인증 전용 토큰 유효기간
        public static final int VERIFICATION_TOKEN_EXPIRY_MINUTES = 5; // 5 minutes
        // 패스워드리스 API 폴링 타임아웃 시간
        public static final long X1280_API_POLLING_TIMEOUT_MS = 60000L; // 60 seconds
        // 패스워드리스 결과 API 재시도 딜레이 시간
        public static final long POLLING_RETRY_DELAY_MS = 2000L; // 2 seconds
        // 재화 유효기간
        public static final int POP_HISTORY_EXPIRATION_YEARS = 5; // 5 years
        // 후원 취소 가능일
        public static final int AVAILABLE_REQUEST_CANCEL_DAYS = 3; // 3 days
        // 게시글 재화 사용 취소 가능 시간
        public static final long AVAILABLE_REQUEST_CANCEL_MINUTES = 10; // 10 minutes
        // 재화 정산 요청 가능일
        public static final long AVAILABLE_REQUEST_SETTLEMENT_DAYS = 3; // 3 days

        // 조회수 중복 증가 방지 시간
        public static final long VIEW_COOLDOWN_HOURS = 24; // 24 hours
    }

    /**
     * Validation 관련 상수값
     */
    public static class Validation {
        // 댓글 최대 길이
        public static final int COMMENT_MAX_LENGTH = 200;
        // 비밀번호 최소 길이
        public static final int PASSWORD_MIN_LENGTH = 10;
        // 닉네임 최소길이
        public static final int NICKNAME_MIN_LENGTH = 1;
        // 닉네임 최대길이
        public static final int NICKNAME_MAX_LENGTH = 10;
        // 휴대폰 번호 길이
        public static final int PHONE_TOTAL_LENGTH = 11; // 010 + 8 digits
        // 1:1 문의 제목 최소 길이
        public static final int INQUIRY_TITLE_MIN_LENGTH = 2;
        // 1:1 문의 내용 최소 길이
        public static final int INQUIRY_CONTENT_MIN_LENGTH = 10;
        // 파일 용량 최대값
        public static final long MAX_FILE_SIZE = 20 * 1024 * 1024;
        // 계좌번호 길이
        public static final int ACCOUNT_TOTAL_LENGTH = 11; // 502 + 7 digits + 1
        // 인기 게시글 조회수 가중치
        public static final int VIEW_WEIGHT = 1;
        // 인기 게시글 좋아요 가중치
        public static final int LIKE_WEIGHT = 3;
    }

    /**
     * 정규식 관리
     */
    public static class ValidationPattern {
        // 비밀번호: 대문자, 숫자, 특수문자 포함 10자 이상, 공백 불가
        public static final String PASSWORD_PATTERN =
            "^(?=.*[A-Z])(?=.*[0-9])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?])\\S{"
            + Validation.PASSWORD_MIN_LENGTH + ",}$";

        // 닉네임: 한글, 영문, 숫자, 한글 자모음 1-10자
        public static final String NICKNAME_PATTERN =
            "^[a-zA-Z0-9가-힣ㄱ-ㅎㅏ-ㅣ]{" + Validation.NICKNAME_MIN_LENGTH + ","
            + Validation.NICKNAME_MAX_LENGTH + "}$";

        // 전화번호: 01X으로 시작, 뒤에 8자리 숫자
        public static final String PHONE_PATTERN = "^01[0-9][0-9]{8}$";

        // 1:1 문의 제목: 공백 제외 최소 2자 이상
        public static final String INQUIRY_TITLE_PATTERN = "^(\\s*\\S\\s*){2,}$";

        // 1:1 문의 내용: 공백 제외 최소 10자 이상
        public static final String INQUIRY_CONTENT_PATTERN = "^(\\s*\\S\\s*){10,}$";

        // 사용자 실명 :
        public static final String REAL_NAME_PATTERN = "^[가-힣a-zA-Z]{2,20}$";

        // 계좌번호: 502로 시작, 1로 끝나는 총 11자리 숫자
        public static final String ACCOUNT_PATTERN = "^502[0-9]{7}1$";

        // 이름: 한글, 영문
        public static final String NAME_PATTERN = "^[가-힣a-zA-Z]+$";

        // 이메일 :
        public static final String EMAIL_PATTERN = "^[a-zA-Z0-9._-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    }

    /**
     * 에러 메세지 관리
     */
    public static class ErrorMessage {
        public static final String EMAIL_REACTIVATION_ERROR =
            "탈퇴 후 " + Time.EMAIL_REACTIVATION_COOLDOWN_DAYS + "일 동안은 재가입이 불가능합니다.";
        public static final String COMMENT_LENGTH_ERROR =
            "최대 " + Validation.COMMENT_MAX_LENGTH + "자까지 입력 가능합니다.";
        public static final String PASSWORD_FORMAT_ERROR =
            "비밀번호는 최소 " + Validation.PASSWORD_MIN_LENGTH
            + "자 이상이며, 대소문자, 숫자, 특수문자를 포함해야 합니다.";
        public static final String NICKNAME_FORMAT_ERROR =
            "닉네임은 " + Validation.NICKNAME_MIN_LENGTH + "자 이상 "
            + Validation.NICKNAME_MAX_LENGTH + "자 이하의 한글, 영문, 숫자만 가능합니다.";
        public static final String PHONE_FORMAT_ERROR =
            "휴대폰 번호는 01X으로 시작하는 " + Validation.PHONE_TOTAL_LENGTH + "자리 숫자여야 합니다.";
        public static final String INQUIRY_TITLE_ERROR =
                "제목은 공백 제외 최소 " + Validation.INQUIRY_TITLE_MIN_LENGTH + "자 이상이어야 합니다.";
        public static final String INQUIRY_CONTENT_ERROR =
                "내용은 공백 제외 최소 " + Validation.INQUIRY_CONTENT_MIN_LENGTH + "자 이상이어야 합니다.";
        public static final String ACCOUNT_FORMAT_ERROR =
            "계좌 번호는 502로 시작하며 1로 끝나는 " + Validation.ACCOUNT_TOTAL_LENGTH + "자리 숫자여야 합니다.";
        public static final String NAME_PATTERN_ERROR =
                "이름은 한글, 영문만 입력 가능합니다.";
        public static final String REAL_NAME_PATTERN_ERROR =
                "이름은 한글 또는 영문으로 2~20자 내외로 입력해 주세요. (특수문자, 숫자 제외)";
        public static final String EMAIL_PATTERN_ERROR =
                "이메일 형식이 올바르지 않습니다.";
    }

    /**
     * 구글 OAuth2 관련 상수값
     */
    public static class OAuth2 {
        public static final String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "oauth2_auth_request";
        public static final String REDIRECT_URI_PARAM_COOKIE_NAME = "redirect_uri";
        public static final int cookieExpireSeconds = 180;
        public static final String LINKING_USER_EMAIL_COOKIE_NAME = "linking_user_email";
    }

    /**
     * Spotlight 관련 상수값
     */
    public static class Spotlight {
        // Spotlight 게시글 작성 시 최소 재화
        public static final int MIN_POP_AMOUNT = 1000;
        // 캐러셀 노출 시 차감 재화
        public static final int CAROUSEL_VIEW_COST = 1;
        // 상세 조회 시 차감 재화
        public static final int DETAIL_VIEW_COST = 5;
        // 메인 캐러셀 표시 개수
        public static final int CAROUSEL_DISPLAY_COUNT = 10;
    }

    /**
     * Redis Key 관련
     */
    public static class Redis {
        public static final String RATE_LIMIT_PREFIX = "rate:limit:email-search:"; // 이메일 찾기 발송 제한 키
        public static final String KEY_PREFIX = "board:view:"; // 조회수 중복 제한 키
        public static final String RANKING_MONTH_PREFIX = "ranking:monthly:"; // 월간 게시글 랭킹(메인페이지)
        public static final String RANKING_WEEK_PREFIX = "ranking:weekly:";
        public static final String SPOTLIGHT_CAROUSEL_PREFIX = "spotlight:carousel:"; // 캐러셀 노출 중복 제한 키
        public static final String SPOTLIGHT_DETAIL_PREFIX = "spotlight:detail:"; // 상세 조회 중복 제한 키
    }



    /**
     * 유튜브 URL 확인
     */
    public static final String YOUTUBE_REGEX =
            "(?:https?://)?(?:www\\.)?(?:youtube\\.com/watch\\?v=|youtu\\.be/)([a-zA-Z0-9_-]{11})";


}
