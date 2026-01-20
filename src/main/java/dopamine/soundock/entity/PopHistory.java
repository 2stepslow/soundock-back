package dopamine.soundock.entity;

import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Table(name = "pop_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PopHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pop_history_id")
    private Integer popHistoryId;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "pop_status")
    @Enumerated(value = EnumType.STRING)
    PopStatus popStatus;

    @Column(name = "requested_at", nullable = false)
    @CreatedDate
    private LocalDateTime requestAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "canceled_at")
    private LocalDateTime canceledAt;

    @Column(name = "target")
    @Enumerated(value = EnumType.STRING)
    PopTarget popTarget;

    @ManyToOne
    @JoinColumn(name = "board_id", nullable = true)
    private Board board;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "related_user_id", nullable = true)
    private User relatedUser;

}
