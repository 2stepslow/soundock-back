package dopamine.soundock.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum CategoryType {
    SHOWCASE,
    PLAYLISTS,
    SPOTLIGHT,
    COMMUNITY,
    REVIEWS,
    NOTICE;

    @JsonCreator
    public static CategoryType fromString(String categoryType) {
        return Arrays.stream(CategoryType.values())
                .filter(e -> e.name().equalsIgnoreCase(categoryType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));
    }
}
