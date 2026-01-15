package dopamine.soundock.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "boards")
@Entity
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

    @CreatedDate
    @Column(name = "deleted_at")
    private LocalDateTime deletedDateTime;

    @Column(name = "file_url", nullable = true)
    private String fileUrl;

    @Column(name = "is_featured", nullable = false)
    private boolean isFeatured;

    @Column(name = "featured_expired_at", nullable = true)
    private LocalDateTime featuredExpiredDateTime;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    // Board 테이블의 category_id 필드를 연결하는거임
    private Category category;
}
