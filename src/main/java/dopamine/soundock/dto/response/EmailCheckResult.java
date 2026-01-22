package dopamine.soundock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EmailCheckResult {
    private final boolean available;
    private final String message;
    private final boolean needsCleanup;  // 30일 지난 탈퇴 유저 존재 여부

    public static EmailCheckResult available() {
        return new EmailCheckResult(true, "사용 가능한 이메일입니다.", false);
    }

    public static EmailCheckResult unavailable(String message) {
        return new EmailCheckResult(false, message, false);
    }

    public static EmailCheckResult availableWithCleanup() {
        return new EmailCheckResult(true, "사용 가능한 이메일입니다.", true);
    }
}
