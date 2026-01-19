package dopamine.soundock.repository;

import dopamine.soundock.entity.AccessTokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface AccessTokenBlacklistRepository extends JpaRepository<AccessTokenBlacklist,Integer> {
    Optional<AccessTokenBlacklist> findByAccessToken(String token);

    boolean existsByAccessToken(String token);

    @Transactional
    void deleteAllByExpirationAtBefore(LocalDateTime dateTime);
}
