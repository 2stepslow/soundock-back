package dopamine.soundock.repository;

import dopamine.soundock.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    @Transactional
    void deleteByUserId(int userId);

    @Transactional
    // expirationAt이 지금 시간보다 이전인 모든 데이터를 삭제한다.
    void deleteAllByExpirationAtBefore(LocalDateTime dateTime);

    Optional<RefreshToken> findByToken(String token);
    boolean existsByToken(String token);
}
