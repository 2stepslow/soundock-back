package dopamine.soundock.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentCountResponse {
    private CommentResponse commentResponse;
    private Integer countComment;
}
