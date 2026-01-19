package dopamine.soundock.repository;

import dopamine.soundock.entity.TossPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TossPaymentRepository extends JpaRepository <TossPayment, Integer> {
}
