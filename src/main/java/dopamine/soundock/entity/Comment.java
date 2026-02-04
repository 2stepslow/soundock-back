package dopamine.soundock.entity;

import dopamine.soundock.dto.request.CommentCreateRequest;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Builder
@Setter
@Getter
@DynamicInsert
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "comments")
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Integer commentId;

    @Column(name = "content")
    private String content;

    @Column(name = "like_count")
    @ColumnDefault("0")
    @Min(0)
    private int likeCount;

//    @Column(name = "parent_comment_id", nullable = false)
//    @ColumnDefault("'0'")
//    private Integer parentId;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdDateTime;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedDateTime;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "board_id")
    private Board board;

    @Formula("(SELECT COUNT(c.comment_id) FROM comments c WHERE c.board_id = board_id AND c.is_deleted = FALSE)")
    private Integer countComment;

    public void updateComment(CommentCreateRequest updateRequest){
        this.content = updateRequest.getContent();
    }

    public void increaseLike(){
        this.likeCount += 1;
    }
    public void decreaseLike(){
        if (likeCount > 0) {
            this.likeCount -= 1;
        }
    }
}
