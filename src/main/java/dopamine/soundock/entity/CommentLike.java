package dopamine.soundock.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "comment_likes",
        uniqueConstraints = {
        @UniqueConstraint(
                columnNames = {"user_id", "comment_id"}
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"user", "comment"})
public class CommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer commentLikeId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "comment_id", nullable = false)
    private Comment comment;
}
