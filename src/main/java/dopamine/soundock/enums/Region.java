package dopamine.soundock.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Region {
    SEOUL(60, 127),
    INCHEON(54, 125),
    GYEONGGI_NORTH(61, 130),
    GYEONGGI_SOUTH(60, 120),
    DAEJEON(68, 100),
    DAEGU(89, 90),
    BUSAN(97, 74),
    GWANGJU(58, 74),
    GANGWON(73, 134),
    JEJU(53, 38);

    private final int nx;
    private final int ny;
}