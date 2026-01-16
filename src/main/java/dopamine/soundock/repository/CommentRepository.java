package dopamine.soundock.repository;

import dopamine.soundock.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface CommentRepository extends JpaRepository<Comment, Integer> {
    List<Comment> findByBoardBoardId(Integer boardId);
}
