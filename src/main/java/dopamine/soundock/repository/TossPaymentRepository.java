package dopamine.soundock.repository;

import dopamine.soundock.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TossPaymentRepository extends JpaRepository <TossPayment, Integer> {
    Optional<TossPayment> findByPaymentKey(String paymentKey);
    Optional<TossPayment> findByOrderId(String OrderId);
}
