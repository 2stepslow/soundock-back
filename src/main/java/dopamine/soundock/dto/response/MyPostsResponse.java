package dopamine.soundock.dto.response;


import dopamine.soundock.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "내가 쓴 게시글 조회")
public class MyPostsResponse {
    @Schema(description = "게시글 id")
    private Integer boardId;

    @Schema(description = "게시판 카테고리")
    private Category category;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 작성일")
    private LocalDateTime createdDateTime;

    @Schema(description = "게시글 조회수")
    private int views;

    @Schema(description = "게시글 추천수")
    private int likes;
}
