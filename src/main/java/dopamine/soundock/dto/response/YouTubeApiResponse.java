package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
        private Thumbnails thumbnails;
    }

    @Data
    public static class ContentDetails {
        private Integer itemCount;
    }

    @Data
    public static class Thumbnails {
        @JsonProperty("default")
        private Thumbnail defaultThumbnail;

        private Thumbnail medium;
        private Thumbnail high;
    }

    @Data
    public static class Thumbnail {
        private String url;
        private Integer width;
        private Integer height;
    }

    @Data
    public static class PageInfo {
        private Integer totalResults;
        private Integer resultsPerPage;
    }
}
