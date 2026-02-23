package dopamine.soundock.service;

import dopamine.soundock.dto.response.FileUploadResponse;
import dopamine.soundock.entity.Announcement;
import dopamine.soundock.entity.AnnouncementAttachment;
import dopamine.soundock.enums.FileType;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.AnnouncementAttachmentRepository;
import dopamine.soundock.repository.AnnouncementRepository;
import dopamine.soundock.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

// 작성 게시글 DB 저장완료 후 요청 할 파일 저장 관련 서비스
@Service
@RequiredArgsConstructor
public class AttachmentAfterCommitService {

    private final AnnouncementRepository announcementRepository;
    private final AnnouncementAttachmentRepository announcementAttachmentRepository;
    private final S3Service s3Service;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void uploadAndSaveAnnouncementAttachments(Integer announceId, List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        try {
            Announcement announcement = announcementRepository.findById(announceId)
                    .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));

            List<FileUploadResponse> uploadedFiles = s3Service.uploadFiles(files);

            for (FileUploadResponse fileResponse : uploadedFiles) {
                FileType fileType = fileResponse.getIsImage() ? FileType.IMAGE : FileType.FILE;

                AnnouncementAttachment attachment = AnnouncementAttachment.builder()
                        .announcement(announcement)
                        .fileUrl(fileResponse.getFileUrl())
                        .fileKey(fileResponse.getFileKey())
                        .fileType(fileType)
                        .originalFilename(fileResponse.getOriginalFilename())
                        .build();

                announcementAttachmentRepository.save(attachment);
            }

        } catch (IOException | ResourceNotFoundException e) {
            announcementRepository.deactivateById(announceId);
            return;
        }
    }
}