package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.LikeBoard;
import dopamine.soundock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardLikeRepository extends JpaRepository<LikeBoard,Integer> {
    Optional<LikeBoard> findByUserAndBoard(User user, Board board);

}
