package dopamine.soundock.repository;

import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
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

    // 후원 내역 조회(후원자, 수혜자 모두 사용)
    @EntityGraph(attributePaths = {"board", "relatedUser"})
    List<PopHistory> findByUserAndCreatedDatetimeIsNotNullAndPopTarget(User user, PopTarget targets);

    // 수혜자의 id와 후원 popHistory의 transactionId와 일치하는 내역 조회
    Optional<PopHistory> findByTransactionIdAndPopTarget(String transactionId, PopTarget popTarget);

    // 후원 취소 요청 시 유저가 후원한 내역 조회에 사용
    @Query("SELECT p FROM PopHistory p " +
            "WHERE p.user = :userId " +
            "AND p.popHistoryId = :popHistoryId " +
            "AND p.createdDatetime > :cutOffDay "  +
            "AND p.popStatus = 'COMPLETED' " +
            "AND p.popTarget = 'DONATION' " +
            "AND p.canceledDatetime IS NULL ")
    Optional<PopHistory> findUserDonationHistory(
            @Param("userId") Integer userId,
            @Param("popHistoryId") Integer popHistoryId,
            @Param("cutOffDay") LocalDateTime cutOffDay);
}
