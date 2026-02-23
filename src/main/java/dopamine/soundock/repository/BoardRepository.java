package dopamine.soundock.repository;

import dopamine.soundock.entity.Board;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.CategoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
    Page<Board> findByDeletedDateTimeIsNullAndCategoryCategoryType(CategoryType categoryType, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "category", "playlist", "attachments"})
    List<Board> findAllByBoardIdInAndDeletedDateTimeIsNull(List<Integer> boardIds);

    @Modifying
    @Query("UPDATE Board b SET b.likes = b.likes + 1 WHERE b.boardId = :boardId")
    void increaseLikes(@Param("boardId") Integer boardId);

    @Modifying
    @Query("UPDATE Board b SET b.likes = b.likes - 1 WHERE b.boardId = :boardId")
    void decreaseLikes(@Param("boardId") Integer boardId);

    // 쿼리 실행 후 영속성 컨텍스트를 비워 데이터 불일치 방지
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Board b SET b.views = b.views + 1 WHERE b.boardId = :id")
    void incrementViews(@Param("id") Integer boardId);

    @EntityGraph(attributePaths = {"playlist", "playlist.items", "category", "user"})
    @Query("SELECT b FROM Board b WHERE b.boardId = :boardId AND b.deletedDateTime IS NULL")
    Optional<Board> findByIdWithPlaylist(@Param("boardId") Integer boardId);


    // 검색기능 - 글제목
    @Query("SELECT b FROM Board b WHERE b.category.categoryType = :categoryType AND b.deletedDateTime IS NULL AND b.title LIKE :pattern")
    Page<Board> searchByTitle(@Param("categoryType") CategoryType categoryType,
                              @Param("pattern") String pattern,
                              Pageable pageable);

    // 검색기능 - 닉네임
    @Query("SELECT b FROM Board b WHERE b.category.categoryType = :categoryType AND b.deletedDateTime IS NULL AND b.user.nickname LIKE :pattern")
    Page<Board> searchByNickname(@Param("categoryType") CategoryType categoryType,
                                 @Param("pattern") String pattern,
                                 Pageable pageable);


    // 내가 쓴 게시글 조회 (삭제된 글 제외)
    Page<Board> findByUserAndIsDeletedFalse(User user, Pageable pageable);
}