package dopamine.soundock.repository;

import dopamine.soundock.entity.User;
import dopamine.soundock.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {
    Optional<VerificationToken> findByToken(String token);

    @Transactional
    void deleteByUser(User user);
}
