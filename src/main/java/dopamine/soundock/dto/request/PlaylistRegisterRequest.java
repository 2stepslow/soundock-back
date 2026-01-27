package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플레이리스트 등록 요청")
public class PlaylistRegisterRequest {
    @Schema(description = "유튜브 플레이리스트 고유 ID")
    @NotBlank(message = "유튜브 플레이리스트 ID는 필수입니다.")
    private String youtubeListId;

    @Schema(description = "플레이리스트 제목")
    @NotBlank(message = "플레이리스트 제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이내여야 합니다.")
    private String title;

    @Schema(description = "썸네일 이미지 URL")
    @NotBlank
    private String thumbnailUrl;

    @Schema(description = "포함된 곡 수")
    @NotNull(message = "포함된 곡 수는 필수입니다.")
    @PositiveOrZero(message = "곡 수는 0개 이상이어야 합니다.")
    private Integer itemCount;
}
