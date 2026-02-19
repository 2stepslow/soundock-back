package dopamine.soundock.repository;

import dopamine.soundock.dto.response.MyPostLikesResponse;
import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.LikeBoard;
import dopamine.soundock.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BoardLikeRepository extends JpaRepository<LikeBoard,Integer> {
    Optional<LikeBoard> findByUserAndBoard(User user, Board board);

    // 내가 좋아요 한 게시글 조회
    @EntityGraph(attributePaths = {"board", "board.category", "board.user"})
    Page<LikeBoard> findByUser(User user, Pageable pageable);
}
