package dopamine.soundock.repository;

import dopamine.soundock.entity.Category;
import dopamine.soundock.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@EnableJpaRepositories
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    Optional<Category> findByCategoryType(CategoryType categoryType);
}
