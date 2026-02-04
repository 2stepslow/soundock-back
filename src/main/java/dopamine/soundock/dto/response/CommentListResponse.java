package dopamine.soundock.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentListResponse {
    private List<CommentResponse> commentResponse;
    private Integer countComment;
}
