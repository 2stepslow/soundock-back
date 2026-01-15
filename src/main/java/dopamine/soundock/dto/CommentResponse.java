package dopamine.soundock.dto;

import dopamine.soundock.entity.Comment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CommentResponse {
    private String nickname;
    private String content;
    private LocalDateTime createdDateTime;

    public static CommentResponse from(Comment comment){
        return CommentResponse.builder()
                .nickname(comment.getUser().getNickname())
                .content(comment.getContent())
                .createdDateTime(comment.getCreatedDateTime())
                .build();
    }
}
