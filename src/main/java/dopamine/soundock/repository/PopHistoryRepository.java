package dopamine.soundock.repository;

import dopamine.soundock.entity.PopHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.Optional;

@EnableJpaRepositories
public interface PopHistoryRepository extends JpaRepository<PopHistory, Integer> {
    Optional<PopHistory> findByOrderId(String orderId);
}
