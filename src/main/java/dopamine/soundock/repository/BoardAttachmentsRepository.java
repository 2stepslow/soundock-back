package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.BoardAttachments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BoardAttachmentsRepository extends JpaRepository<BoardAttachments, Integer> {
    List<BoardAttachments> findByBoard(Board board);
    List<BoardAttachments> findByBoardOrderBySequenceAsc(Board board);
}