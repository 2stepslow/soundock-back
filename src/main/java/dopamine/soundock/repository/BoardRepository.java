package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 상위 카테고리-하위 카테고리-삭제되지않은 해당 board id에 해당하는 게시글 조회
    Optional<Board> findByBoardIdAndDeletedDateTimeIsNullAndCategoryCategoryTypeAndCategoryParentId(Integer boardId, String categoryType, Integer parentId);
    // 상위 카테고리-하위 카테고리의 삭제되지 않은 게시글 조회
    List<Board> findByDeletedDateTimeIsNullAndCategoryParentIdAndCategoryCategoryType(Integer parentId, String categoryType);
}