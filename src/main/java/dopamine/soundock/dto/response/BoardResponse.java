package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.entity.Category;
import dopamine.soundock.enums.CategoryType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

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
    private boolean isLiked;
    private int countComment;
    private CategoryType categoryType;
    private String linkUrl;
    private String imageUrl;
    private List<String> imageUrls;
    private List<Integer> imageIds;
    private String attachmentUrl;

}
