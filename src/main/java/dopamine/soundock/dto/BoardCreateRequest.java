package dopamine.soundock.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder
public class BoardCreateRequest {
    private String title;
    private String content;
    private String fileUrl;
}
