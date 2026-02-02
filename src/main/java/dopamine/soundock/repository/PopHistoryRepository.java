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
    List<PopHistory> findByUserAndCreatedDatetimeIsNotNullAndPopTargetOrderByCreatedDatetimeDesc(User user, PopTarget targets);

    // 수혜자의 id와 후원 popHistory의 transactionId와 일치하는 내역 조회
    Optional<PopHistory> findByTransactionIdAndPopTarget(String transactionId, PopTarget popTarget);

    // popTarget이 FEATURED_BOARD, boardId와 일치하는 내역 조회
    Optional<PopHistory> findByPopTargetAndBoardBoardId(PopTarget popTarget, Integer boardId);

    @Query("SELECT p FROM PopHistory p " +
            "WHERE p.user.id = :userId " +
            "AND p.popTarget = :popTarget " +
            "AND p.popStatus = :popStatus " +
            "AND p.requestedDatetime IS NULL " +
            "AND p.approvedDatetime IS NULL " +
            "AND p.createdDatetime <= :availableDay " +
            "AND ORDER BY p.createdDatetime DESC ")
    List<PopHistory> findAvailableSettlement(
            @Param("userId") Integer userId,
            @Param("popTarget") PopTarget popTarget,
            @Param("popStatus") PopStatus popStatus,
            @Param("availableDay") LocalDateTime availableDay);

    @Query("SELECT p FROM PopHistory p " +
    "WHERE p.user.id = :userId " +
    "AND p.popStatus IN ('SETTLEMENT_REQUEST', 'SETTLEMENT_COMPLETED') " +
    "AND p.canceledDatetime IS NULL ")
    List<PopHistory> findMySettlementList(@Param("userId") Integer userId);
}
