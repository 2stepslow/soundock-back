package dopamine.soundock.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardResponse{
    private Integer userId;
    private Integer boardId;
    private String title;
    private String content;
    private String nickname;
    private int views;
    private int likes;
    private LocalDateTime createdDateTime;
    private String fileUrl;
    private boolean isliked;
}
