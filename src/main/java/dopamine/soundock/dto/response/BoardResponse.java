package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdDateTime;
    private String fileUrl;
    private boolean isLiked;
    private int countComment;
}
