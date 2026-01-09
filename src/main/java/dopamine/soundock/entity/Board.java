package dopamine.soundock.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "boards")
@Entity
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int Id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "view_count", nullable = false)
    private int views;

    @Column(name = "like_count", nullable = false)
    private int likes;

    @CreatedDate
    @Column(name = "created_at", nullable = true)
    private LocalDateTime createdDateTime;

    @CreatedDate
    @Column(name = "updated_at", nullable = true)
    private LocalDateTime updatedDateTime;

    @CreatedDate
    @Column(name = "deleted_at", nullable = true)
    private LocalDateTime deletedDateTime;

    @Column(name = "file_url", nullable = true)
    private String fileUrl;

    @Column(name = "is_Featured", nullable = false)
    private boolean isFeatured;

    @Column(name = "expired_featured_at", nullable = true)
    private LocalDateTime expiredFeaturedDateTime;
}
