package dopamine.soundock.repository;

import dopamine.soundock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndIsDeletedFalse(String email);

    List<User> findByNameAndPhoneNumberAndIsDeletedFalse(String name, String phoneNumber);

    void deleteByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.popBalance = u.popBalance + :amount WHERE u.email = :email")
    int increasePopBalance(@Param("email") String email, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u SET u.popBalance = u.popBalance - :amount WHERE u.email = :email AND u.popBalance >= :amount")
    int decreasePopBalance(@Param("email") String email, @Param("amount") int amount);

    @Modifying(clearAutomatically = true)
    // COALESCE 함수 : pop_balance가 null일 경우 0으로 처리함
    @Query("UPDATE User u SET u.popBalance = COALESCE(u.popBalance, 0) + :amount " +
            "WHERE u.id = :userId")
    int incrementUserPoint(@Param("userId") Integer userId, @Param("amount") Integer amount);
}
