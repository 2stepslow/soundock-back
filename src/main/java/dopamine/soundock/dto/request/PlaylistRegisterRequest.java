package dopamine.soundock.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "플레이리스트 등록 요청")
public class PlaylistRegisterRequest {
    @Schema(description = "유튜브 플레이리스트 고유 ID")
    private String youtubeListId;

    @Schema(description = "플레이리스트 제목")
    private String title;

    @Schema(description = "썸네일 이미지 URL")
    private String thumbnailUrl;

    @Schema(description = "포함된 곡 수")
    private Integer itemCount;
}
