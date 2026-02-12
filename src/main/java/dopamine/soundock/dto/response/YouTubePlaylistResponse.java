package dopamine.soundock.dto.response;

import dopamine.soundock.entity.Playlist;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "유튜브 플레이리스트 응답 정보")
public class YouTubePlaylistResponse {
    @Schema(description = "플레이리스트 PK ID")
    private Integer playlistId;

    @Schema(description = "플레이리스트 고유 ID")
    private String youtubeListId;

    @Schema(description = "유튜브 플레이리스트 제목")
    private String title;

    @Schema(description = "유튜브 플레이리스트 썸네일 URL")
    private String thumbnailUrl;

    @Schema(description = "유튜브 플레이리스트 내의 곡 수")
    private Integer itemCount;

    public static YouTubePlaylistResponse fromEntity(Playlist playlist) {
        return YouTubePlaylistResponse.builder()
                .playlistId(playlist.getPlaylistId())
                .youtubeListId(playlist.getYoutubeListId())
                .title(playlist.getTitle())
                .thumbnailUrl(playlist.getThumbnailUrl())
                .itemCount(playlist.getItemCount())
                .build();
    }
}
