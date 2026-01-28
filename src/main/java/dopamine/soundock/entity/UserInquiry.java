package dopamine.soundock.entity;

import dopamine.soundock.enums.CommentStatus;
import dopamine.soundock.enums.InquiryStatus;
import dopamine.soundock.enums.InquiryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@Builder
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_inquiries")
public class UserInquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_inquiry_id")
    private Integer userInquiryId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    private String content;

    @Column(name = "inquiry_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private InquiryType inquiryType;

    @Column(name = "file_url")
    private String fileUrl;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "inquiry_status")
    @Enumerated(EnumType.STRING)
    private InquiryStatus inquiryStatus;

    @Column(name = "admin_comment")
    private String adminComment;

    @Column(name = "comment_status")
    @Enumerated(EnumType.STRING)
    private CommentStatus commentStatus;

    @Column(name = "comment_created_at")
    private LocalDateTime commentCreatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id")
    private User admin;

    /**
     * 관리자 답변 전용 메서드
     */
    public void answerInquiry(User admin, String comment) {
        this.admin = admin;
        this.adminComment = comment;
        this.commentStatus = CommentStatus.COMPLETED;
        this.inquiryStatus = InquiryStatus.COMPLETED;
        this.commentCreatedAt = LocalDateTime.now();
    }

}
