package dopamine.soundock.entity;


import dopamine.soundock.enums.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)

public class Notifications {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Integer notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "received_user_id", nullable = false)
    private User receivedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sending_user_id", nullable = false)
    private User sendingUser;

    @Column(name = "notification_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType notificationType;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id", nullable = true)
    private Board board;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdDatetime;

    @Column(name = "read_at", nullable = true)
    private LocalDateTime readAt;

}
