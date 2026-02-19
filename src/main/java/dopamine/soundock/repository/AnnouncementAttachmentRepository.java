package dopamine.soundock.repository;

import dopamine.soundock.entity.AnnouncementAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnouncementAttachmentRepository extends JpaRepository<AnnouncementAttachment, Integer> {

    // 공지사항별 첨부파일 조회
    List<AnnouncementAttachment> findByAnnouncement_AnnounceIdOrderByAnnouncementAttachmentIdAsc(Integer announceId);

    // 공지사항별 첨부파일 삭제
    void deleteByAnnouncement_AnnounceId(Integer announceId);
}
