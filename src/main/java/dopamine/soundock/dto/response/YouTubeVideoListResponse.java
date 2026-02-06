package dopamine.soundock.dto.response;

import lombok.Data;

import java.util.List;

@Data
public class YouTubeVideoListResponse {
    private List<VideoItem> items;
    private String nextPageToken; // 페이징 처리용 토큰
    private PageInfo pageInfo;

    @Data
    public static class VideoItem {
        private String id;
        private VideoSnippet snippet;
    }

    @Data
    public static class VideoSnippet {
        private String title;       // 동영상 제목
        private YoutubeThumbnailsDTO.Thumbnails thumbnails;
        private Integer position;   // 플레이리스트 내 순서 (0, 1, 2...)
        private ResourceId resourceId; // 실제 비디오 정보가 담긴 곳
    }

    @Data
    public static class ResourceId {
        private String videoId; // 우리가 재생할 때 필요한 실제 유튜브 비디오 ID!!
    }

    @Data
    public static class PageInfo {
        private Integer totalResults;
        private Integer resultsPerPage;
    }
}
