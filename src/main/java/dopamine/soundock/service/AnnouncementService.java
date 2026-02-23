package dopamine.soundock.service;

import dopamine.soundock.dto.response.AnnouncementResponse;
import dopamine.soundock.entity.Announcement;
import dopamine.soundock.entity.AnnouncementAttachment;
import dopamine.soundock.enums.AnnounceType;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.AnnouncementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class AnnouncementService {

    private static final int PAGE_SIZE = 15;

    private final AnnouncementRepository announcementRepository;

    // 리스트 조회
    public Page<AnnouncementResponse> getAnnouncements(int page) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Announcement> result = announcementRepository.findActiveForList(pageable);

        return result.map(a -> AnnouncementResponse.builder()
                .announceId(a.getAnnounceId())
                .announceType(a.getAnnounceType())
                .title(a.getTitle())
                .linkUrl(a.getLinkUrl())
                .priority(a.getPriority())
                .isActive(a.getIsActive())
                .startedAt(a.getStartedAt())
                .endedAt(a.getEndedAt())
                .createdAt(a.getCreatedAt())
                .build()
        );
    }

    // 상세 조회
    public AnnouncementResponse getAnnouncementDetail(Integer announceId) {

        Announcement a = announcementRepository.findByAnnounceIdAndIsActiveTrue(announceId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));

        return AnnouncementResponse.builder()
                .announceId(a.getAnnounceId())
                .announceType(a.getAnnounceType())
                .title(a.getTitle())
                .content(a.getContent())
                .linkUrl(a.getLinkUrl())
                .priority(a.getPriority())
                .isActive(a.getIsActive())
                .startedAt(a.getStartedAt())
                .endedAt(a.getEndedAt())
                .createdAt(a.getCreatedAt())
                .fileUrls(
                        a.getAttachments().stream()
                                .map(AnnouncementAttachment::getFileUrl)
                                .toList()
                )
                .attachmentIds(
                        a.getAttachments().stream()
                                .map(AnnouncementAttachment::getAnnouncementAttachmentId)
                                .toList()
                )
                .build();
    }

    // 타입별 priority=0 상세 조회 (푸터용)
    public AnnouncementResponse getPinnedAnnouncementByType(AnnounceType announceType) {

        Announcement a = announcementRepository
                .findFirstByAnnounceTypeAndIsActiveTrueAndPriorityOrderByCreatedAtDesc(announceType, 0)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항이 없습니다."));

        return AnnouncementResponse.builder()
                .announceId(a.getAnnounceId())
                .announceType(a.getAnnounceType())
                .title(a.getTitle())
                .content(a.getContent())
                .linkUrl(a.getLinkUrl())
                .priority(a.getPriority())
                .isActive(a.getIsActive())
                .startedAt(a.getStartedAt())
                .endedAt(a.getEndedAt())
                .createdAt(a.getCreatedAt())
                .fileUrls(
                        a.getAttachments().stream()
                                .map(AnnouncementAttachment::getFileUrl)
                                .toList()
                )
                .attachmentIds(
                        a.getAttachments().stream()
                                .map(AnnouncementAttachment::getAnnouncementAttachmentId)
                                .toList()
                )
                .build();
    }


}
