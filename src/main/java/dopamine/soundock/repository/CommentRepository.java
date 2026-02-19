package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByIsDeletedIsFalseAndBoard(Board board);

    Integer countByIsDeletedIsFalseAndBoard(Board board);

    // 내가 쓴 댓글 조회 (삭제된 댓글 제외)
    @EntityGraph(attributePaths = {"board", "board.category"})
    Page<Comment> findByUserAndIsDeletedFalse(User user, Pageable pageable);
}
