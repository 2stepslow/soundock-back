package dopamine.soundock.repository;

import dopamine.soundock.entity.Comment;
import dopamine.soundock.entity.CommentLike;
import dopamine.soundock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface CommentLikeRepository extends JpaRepository<CommentLike, Integer> {
    // 유저가 댓글에 좋아요한 유무 확인
    boolean existsByCommentAndUser(Comment comment, User user);

    // 유저가 좋아요한 댓글 목록 조회
    List<CommentLike> findAllByUserAndCommentIn(User user, List<Comment> comment);

    // 유저가 좋아요 취소 시 commentLike 테이블에서 삭제
    void deleteByCommentAndUser(Comment comment,User user);
}
