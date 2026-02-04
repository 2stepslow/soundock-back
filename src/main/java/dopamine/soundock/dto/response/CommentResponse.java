package dopamine.soundock.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import dopamine.soundock.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.hibernate.annotations.Formula;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Integer commentId;
    private Integer userId;
    private String nickname;
    private String content;
    private Integer likeCount;
    private boolean toggledLike;
    private Integer countComment;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime createdDatetime;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    private LocalDateTime updatedDatetime;

    public static CommentResponse from(Comment comment){
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .countComment(comment.getCountComment())
                .createdDatetime(comment.getCreatedDateTime())
                .updatedDatetime(comment.getUpdatedDateTime())
                .build();
    }

    public static CommentResponse fromForLoginUser(Comment comment, boolean toggledLike){
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .likeCount(comment.getLikeCount())
                .toggledLike(toggledLike)
                .createdDatetime(comment.getCreatedDateTime())
                .updatedDatetime(comment.getUpdatedDateTime())
                .build();
    }
}
