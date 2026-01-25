package dopamine.soundock.repository;

import dopamine.soundock.entity.Playlist;
import dopamine.soundock.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {
    // 특정 유저가 저장한 모든 플레이리스트 목록 조회 (마이페이지용)
    List<Playlist> findAllByUser(User user);

    // 유저가 이미 특정 플레이리스트를 저장했는지 확인 (중복 저장 방지)
    Optional<Playlist> findByUserAndYoutubeListId(User user, String youtubeListId);

    // 특정 플레이리스트 삭제 (마이페이지에서 삭제 시 사용)
    void deleteByUserAndPlaylistId(User user, Integer PlaylistId);

    boolean existsByUserAndPlaylistId(User user, Integer playlistId);
}
