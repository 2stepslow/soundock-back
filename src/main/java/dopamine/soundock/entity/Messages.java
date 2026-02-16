package dopamine.soundock.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Messages {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "message_id")
    private Integer messageId;

    @Column(name = "content")
    private String content;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sending_user_id", nullable = false)
    private User sendingUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_user_id", nullable = false)
    private User receivedUser;

    @Column(name = "read_at", nullable = true)
    private LocalDateTime readAt;


}
