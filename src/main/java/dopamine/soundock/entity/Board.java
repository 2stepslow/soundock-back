package dopamine.soundock.entity;

import dopamine.soundock.enums.CategoryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "boards")
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Integer boardId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private int views;

    @Column(name = "like_count", nullable = false)
    private int likes;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdDateTime;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedDateTime;

    @Column(name = "deleted_at")
    private LocalDateTime deletedDateTime;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted;

    @Column(name = "link_url", nullable = true)
    private String linkUrl;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;

    @Column(name = "featured_expired_at", nullable = true)
    private LocalDateTime featuredExpiredDateTime;

    @OneToOne
    @JoinColumn(name = "category_id", nullable = false)
    // Board 테이블의 category_id 필드를 연결하는거임
    private Category category;

    @Formula("(SELECT COUNT(c.comment_id) FROM comments c WHERE c.board_id = board_id AND c.is_deleted = FALSE)")
    private int countComment;

    @OneToMany(mappedBy = "board", fetch = FetchType.LAZY)
    @BatchSize(size = 5)
    @OrderBy("sequence ASC")
    private List<BoardAttachments> attachments = new ArrayList<>();
}
