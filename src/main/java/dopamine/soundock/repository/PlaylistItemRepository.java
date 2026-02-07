package dopamine.soundock.repository;

import dopamine.soundock.entity.Playlist;
import dopamine.soundock.entity.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Integer> {

    @Modifying
    void deleteByPlaylist(Playlist playlist);

    List<PlaylistItem> findAllByPlaylistOrderByPositionAsc(Playlist playlist);
}
