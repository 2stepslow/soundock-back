package dopamine.soundock.repository;

import dopamine.soundock.entity.Oauth;
import dopamine.soundock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthRepository extends JpaRepository<Oauth, Integer> {

    // 특정 유저의 OAuth 정보가 있는지 확인 (연동 여부 체크)
    Optional<Oauth> findByUser(User user);

    // 유저와 제공자(GOOGLE)를 함께 조건으로 조회
    Optional<Oauth> findByUserAndProvider(User user, String provider);

    // Oauth2 연동 삭제
    @Modifying
    @Query("DELETE FROM Oauth o WHERE o.user = :user")
    void deleteByUser(User user);
}
