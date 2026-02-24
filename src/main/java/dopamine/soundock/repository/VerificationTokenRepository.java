package dopamine.soundock.repository;

import dopamine.soundock.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Integer> {
    Optional<VerificationToken> findByToken(String token);

    @Modifying
    @Transactional
    void deleteByEmail(String email);

    Optional<VerificationToken> findByEmail(String email);
}
