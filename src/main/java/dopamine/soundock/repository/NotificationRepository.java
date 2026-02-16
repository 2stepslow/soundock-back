package dopamine.soundock.repository;


import dopamine.soundock.entity.Messages;
import dopamine.soundock.entity.Notifications;
import dopamine.soundock.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notifications, Integer> {

    // 읽지 않은 알림 우선 조회 (readAt null이면 0) -> 최신순 (이외 readAt 1)
    @Query("SELECT n FROM Notifications n " +
            "WHERE n.receivedUser.id = :userId " +
            "ORDER BY CASE WHEN n.readAt IS NULL THEN 0 ELSE 1 END, n.createdDatetime DESC")
    Page<Notifications> findByReceivedUserId(@Param("userId") Integer userId, Pageable pageable);

    // 읽지 않은 알림 개수
    @Query("SELECT COUNT(n) FROM Notifications n WHERE n.receivedUser.id = :userId AND n.readAt IS NULL")
    Integer countUnreadNotifications(@Param("userId") Integer userId);

    @Query("SELECT n FROM Notifications n WHERE n.createdDatetime < :date")
    List<Notifications> findByCreatedDatetimeBefore(@Param("date") LocalDateTime date);

    // 중복 알림 작성 방지
    @Query("SELECT COUNT(n) > 0 FROM Notifications n " +
            "WHERE n.sendingUser.id = :sendingUserId AND n.receivedUser.id = :receivedUserId " +
            "AND n.board.id = :boardId AND n.notificationType = :type")
    boolean existsByCondition(@Param("sendingUserId") Integer sendingUserId,
                              @Param("receivedUserId") Integer receivedUserId,
                              @Param("boardId") Integer boardId,
                              @Param("type") NotificationType type);
}
