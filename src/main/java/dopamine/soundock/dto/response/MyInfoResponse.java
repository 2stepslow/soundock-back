package dopamine.soundock.dto.response;

import dopamine.soundock.entity.UserGrade;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "내 정보 조회 응답")
public class MyInfoResponse {
    @Schema(description = "내 이메일 주소")
    private String email;

    @Schema(description = "사용자 실명")
    private String name;

    @Schema(description = "내 닉네임")
    private String nickname;

    @Schema(description = "내 전화번호")
    private String phoneNumber;

    @Schema(description = "유튜브 연동 여부")
    private boolean isYoutubeConnected;

    @Schema(description = "Pop 잔여량")
    private Integer popBalance;

    @Schema(description = "사이트 내 등급")
    private String userGrade;
}
