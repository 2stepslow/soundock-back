package dopamine.soundock.repository;

import dopamine.soundock.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    void deleteByUserId(int userId);

    // expirationAt이 지금 시간보다 이전인 모든 데이터를 삭제한다.
    void deleteAllByExpirationAtBefore(LocalDateTime dateTime);
}
