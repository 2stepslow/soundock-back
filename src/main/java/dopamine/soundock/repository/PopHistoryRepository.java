package dopamine.soundock.repository;

import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Optional;

@EnableJpaRepositories
public interface PopHistoryRepository extends JpaRepository<PopHistory, Integer> {
    Optional<PopHistory> findByOrderIdAndPopStatus(String orderId, PopStatus popStatus);
    // 사용자 구매 내역 조회
    List<PopHistory> findByUserAndPopTargetOrderByCreatedDatetimeDesc(User user, PopTarget popTarget);
    // 사용자 재화 사용 내역 조회
    @EntityGraph(attributePaths = {"board", "relatedUser"})
    List<PopHistory> findByUserAndRequestedDatetimeIsNotNullAndPopTargetIn(User user, List<PopTarget> targets);
}
