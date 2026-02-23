package dopamine.soundock.dto.response;


import dopamine.soundock.enums.AnnounceType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnnouncementResponse {
    private Integer announceId;
    private AnnounceType announceType;

    private String title;
    private String content;
    private String linkUrl;

    private Integer priority;
    private Boolean isActive;

    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private LocalDateTime createdAt;

    private List<String> fileUrls;
    private List<Integer> attachmentIds;
}
