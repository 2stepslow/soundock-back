package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@EnableJpaRepositories
public interface BoardRepository extends JpaRepository<Board, Integer> {
    // 삭제되지 않고 부모,하위 카테고리를 포함한 게시글 조회
    List<Board> findByDeletedDateTimeIsNullAndCategoryIdIn(Collection<Integer> categoryIds);

    List<Board> findByDeletedDateTimeIsNull();
    List<Board> findByDeletedDateTimeIsNullAndCategoryParentIdAndCategoryCategoryType(Integer parentId, String categoryType);
}