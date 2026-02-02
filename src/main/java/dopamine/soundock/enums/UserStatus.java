package dopamine.soundock.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum UserStatus {
    PENDING,
    ACTIVE,
    WARN,
    BLOCKED,
    BANNED,
    QUITTED;

    @JsonCreator
    public static UserStatus fromString(String userStatus) {
        return Arrays.stream(UserStatus.values())
                .filter(e -> e.name().equalsIgnoreCase(userStatus))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 유저 상태입니다."));
    }
}
