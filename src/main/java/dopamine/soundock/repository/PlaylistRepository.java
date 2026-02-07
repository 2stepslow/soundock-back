package dopamine.soundock.repository;

import dopamine.soundock.entity.Playlist;
import dopamine.soundock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {
    // 특정 유저가 저장한 모든 플레이리스트 목록 조회 (마이페이지용)
    List<Playlist> findAllByUser(User user);

    // 유저가 이미 특정 플레이리스트를 저장했는지 확인 (중복 저장 방지)
    boolean existsByUserAndYoutubeListId(User user, String youtubeListId);

    // 삭제할 플레이리스트가 DB에 존재하는지 확인
    Optional<Playlist> findByPlaylistId(Integer playlistId);

    @Modifying
    @Transactional
    void deleteByUser(User user);
}
