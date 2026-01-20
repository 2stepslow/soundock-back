package dopamine.soundock.entity;

import dopamine.soundock.enums.TossPaymentMethod;
import dopamine.soundock.enums.TossPaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Table(name = "toss_payments")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_id")
    private Integer paymentId;

    @OneToOne
    @JoinColumn(name = "pop_history_id", unique = true)
    private PopHistory popHistory;

    @Column(name = "toss_order_id", nullable = false)
    private String tossOrderId; // toss에게 넘겨줄 주문 uuid

    @Column(name = "toss_payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(name = "order_name", nullable = false)
    private String orderName;

    @Column(name = "total_amount", nullable = false)
    private int totalAmount;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "toss_payment_method", nullable = false)
    TossPaymentMethod tossPaymentMethod;

    @Enumerated(value = EnumType.STRING)
    @Column(name = "toss_payment_status", nullable = false)
    TossPaymentStatus tossPaymentStatus;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
