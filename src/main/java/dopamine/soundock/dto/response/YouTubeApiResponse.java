package dopamine.soundock.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class YouTubeApiResponse {
    private List<PlaylistItem> items;
    private PageInfo pageInfo;

    @Data
    public static class PlaylistItem {
        private String id;
        private Snippet snippet;
        private ContentDetails contentDetails;
    }

    @Data
    public static class Snippet {
        private String title;
        private YoutubeThumbnailsDTO.Thumbnails thumbnails;
    }

    @Data
    public static class ContentDetails {
        private Integer itemCount;
    }

    @Data
    public static class PageInfo {
        private Integer totalResults;
        private Integer resultsPerPage;
    }
}
