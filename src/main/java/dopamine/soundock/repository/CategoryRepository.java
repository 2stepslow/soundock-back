package dopamine.soundock.repository;

import dopamine.soundock.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // 카테고리 활성화 여부 확인
    boolean findByIdAndIsActive(Integer id, boolean isActive);
    // 특정 카테고리 밑 모든 자식 카테고리 찾기
    List<Category> findByParentId(Integer categoryId);
    // 부모 카테고리 밑 자식 카테고리 조회
    List<Category> findByParentIdAndCategoryType(Integer parentId, String categoryType);

    // 부모 Id와 이름으로 카테고리 찾기
    Optional<Category> findByParentIdAndSection(Integer parentId, String section);
}
