package dopamine.soundock.repository;

import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface PopHistoryRepository extends JpaRepository<PopHistory, Integer> {
    Optional<PopHistory> findByOrderId(String orderId);
    // 사용자 구매 내역 조회
    List<PopHistory> findByUserOrderByCreatedDatetimeDesc(User user);
    // 사용자 재화 사용 내역 조회
    List<PopHistory> findByUserAndRequestedDatetimeIsNotNullAndPopTargetIn(User user, List<PopTarget> targets);
}
