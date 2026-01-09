package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@EnableJpaRepositories
public interface BoardRepository extends JpaRepository<Board, Integer> {
}
