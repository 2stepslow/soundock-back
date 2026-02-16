package dopamine.soundock.repository;


import dopamine.soundock.entity.Messages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Messages, Integer> {
    // 받은 메세지 조회
    List<Messages> findByReceivedUser_IdOrderByCreatedDatetimeDesc(Integer userId);

    // 보낸 메세지 조회
    List<Messages> findBySendingUser_IdOrderByCreatedDatetimeDesc(Integer userId);

    // 읽지 않은 메세지 개수
    @Query("SELECT COUNT(m) FROM Messages m WHERE m.receivedUser.id = :userId AND m.readAt IS NULL")
    Integer countUnreadMessages(@Param("userId") Integer userId);


    List<Messages> findByCreatedDatetimeBefore(LocalDateTime datetime);

}
