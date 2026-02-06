package dopamine.soundock.dto.response;

import lombok.Data;

@Data
public class YoutubeThumbnailsDTO {
    @Data
    public static class Thumbnail {
        private String url;
        private String width;
        private String height;
    }

    @Data
    public static class Thumbnails {
        private Thumbnail defaultThumbnail;
        private Thumbnail medium;
        private Thumbnail high;
    }
}
