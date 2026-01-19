package dopamine.soundock.dto;

import dopamine.soundock.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse{
    private String title;
    private String content;
    private String nickname;
    private int views;
    private int likes;
    private LocalDateTime createdDateTime;
    private String fileUrl;
}
