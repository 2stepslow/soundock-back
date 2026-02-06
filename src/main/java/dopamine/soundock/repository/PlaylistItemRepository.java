package dopamine.soundock.repository;

import dopamine.soundock.entity.Playlist;
import dopamine.soundock.entity.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Integer> {

    @Modifying
    void deleteByPlaylist(Playlist playlist);
}
