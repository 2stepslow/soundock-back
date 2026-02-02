package dopamine.soundock.dto.response;

import dopamine.soundock.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Integer commentId;
    private Integer userId;
    private String nickname;
    private String content;
    private LocalDateTime createdDateTime;

    public static CommentResponse from(Comment comment){
        return CommentResponse.builder()
                .commentId(comment.getCommentId())
                .userId(comment.getUser().getId())
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .createdDateTime(comment.getCreatedDateTime())
                .build();
    }
}
