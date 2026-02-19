package dopamine.soundock.dto.response;

import dopamine.soundock.enums.CategoryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
@Schema(description = "내가 좋아요 한 게시글 조회")
public class MyPostLikesResponse {
    @Schema(description = "좋아요 id")
    private int postLikeId;

    @Schema(description = "게시판 카테고리")
    private CategoryType categoryType;

    @Schema(description = "좋아요 한 게시글의 id")
    private Integer boardId;

    @Schema(description = "좋아요 한 게시글의 제목")
    private String title;

    @Schema(description = "게시글 작성자의 닉네임")
    private String nickname;

    @Schema(description = "좋아요 한 게시글의 조회수")
    private int views;

    @Schema(description = "좋아요 한 게시글의 추천수")
    private int likes;
}
