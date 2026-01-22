package dopamine.soundock.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class YouTubePlaylistResponse {
    private String playlistId;
    private String title;
    private String thumbnailUrl;
    private Integer itemCount;
}
