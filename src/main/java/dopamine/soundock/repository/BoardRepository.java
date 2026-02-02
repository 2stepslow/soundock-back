package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.Category;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 카테고리-boardId에 해당하는 삭제되지 않은 게시글 조회
    Optional<Board> findByBoardIdAndCategoryCategoryType(Integer boardId, CategoryType categoryType);
    // boardId에 해당하는 삭제된 게시글 조회
    Optional<Board> findByBoardIdAndDeletedDateTimeIsNullAndCategoryCategoryType(Integer boardId, CategoryType categoryType);

    Optional<Board> findByBoardIdAndDeletedDateTimeIsNull(Integer boardId);
    // 카테고리의 삭제되지 않은 게시글 조회
    List<Board> findByDeletedDateTimeIsNullAndCategoryCategoryType(CategoryType categoryType);
}