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

    @Column(name = "toss_order_id")
    private String orderId;

    @Column(name = "change_amount", nullable = false)
    private Integer changeAmount;

    @Column(name = "actual_amount")
    private Integer actualAmount;

    @Column(name = "pop_status")
    @Enumerated(value = EnumType.STRING)
    PopStatus popStatus;

    @Column(name = "created_at", nullable = false)
    @CreatedDate
    private LocalDateTime createdDatetime;

    @Column(name = "requested_at")
    private LocalDateTime requestedDatetime;

    @Column(name = "approved_at")
    private LocalDateTime approvedDatetime;

    @Column(name = "canceled_at")
    private LocalDateTime canceledDatetime;

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

    public void completeChargePayment(PopStatus status, PopTarget target){
        this.popStatus = status;
        this.popTarget = target;
        this.createdDatetime = LocalDateTime.now();
    }
}
