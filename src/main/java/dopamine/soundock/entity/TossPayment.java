package dopamine.soundock.entity;

import dopamine.soundock.dto.response.ConfirmPaymentResponse;
import jakarta.persistence.*;
import lombok.*;

import javax.accessibility.AccessibleContext;
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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pop_history_id", unique = true)
    private PopHistory popHistory;

    @Column(name = "toss_order_id", nullable = false, unique = true)
    private String orderId; // toss에게 넘겨줄 주문 uuid

    @Column(name = "toss_cancel_order_id", unique = true)
    private String cancelId;

    @Column(name = "toss_payment_key", nullable = false, unique = true)
    private String paymentKey;

    @Column(name = "order_name", nullable = true)
    private String orderName;

    @Column(name = "amount", nullable = false)
    private int amount;

    @Column(name = "toss_payment_method", nullable = false)
    private String tossPaymentMethod;

    @Column(name = "toss_payment_status", nullable = false)
    private String tossPaymentStatus;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedDatetime;

    @Column(name = "approved_at")
    private LocalDateTime approvedDatetime;

    public void cancelUpdatePayment(ConfirmPaymentResponse confirmPaymentResponse){
        this.tossPaymentStatus = confirmPaymentResponse.getStatus();
        this.requestedDatetime = confirmPaymentResponse.getRequestedAt().toLocalDateTime();
        this.approvedDatetime = confirmPaymentResponse.getApprovedAt().toLocalDateTime();
    }
}
