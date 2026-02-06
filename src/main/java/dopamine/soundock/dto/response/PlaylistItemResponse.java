package dopamine.soundock.dto.response;

import dopamine.soundock.entity.PlaylistItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "플레이리스트 내 개별 곡 정보")
public class PlaylistItemResponse {

    @Schema(description = "유튜브 비디오 고유 ID")
    private String videoId;

    @Schema(description = "곡 제목")
    private String title;

    @Schema(description = "곡 썸네일 URL")
    private String thumbnailUrl;

    // Entity를 DTO로 변환하는 정적 메서드
    public static PlaylistItemResponse fromEntity(PlaylistItem item) {
        return PlaylistItemResponse.builder()
                .videoId(item.getVideoId())
                .title(item.getTitle())
                .thumbnailUrl(item.getThumbnailUrl())
                .build();
    }
}
