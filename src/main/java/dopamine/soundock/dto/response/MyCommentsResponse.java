package dopamine.soundock.dto.response;

import dopamine.soundock.enums.CategoryType;
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
@Schema(description = "내가 쓴 댓글 조회")
public class MyCommentsResponse {
    @Schema(description = "댓글 id")
    private Integer commentId;

    @Schema(description = "댓글 작성일")
    private LocalDateTime createdDateTime;

    @Schema(description = "게시판 카테고리")
    private CategoryType categoryType;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "게시글 id")
    private Integer boardId;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "댓글 추천수")
    private int likeCount;
}