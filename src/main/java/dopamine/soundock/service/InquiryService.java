package dopamine.soundock.service;

import dopamine.soundock.dto.request.InquiryCreateRequest;
import dopamine.soundock.dto.response.InquiryDetailResponse;
import dopamine.soundock.dto.response.InquirySummaryResponse;
import dopamine.soundock.entity.User;
import dopamine.soundock.entity.UserInquiry;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.UserInquiryRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {
    private final UserInquiryRepository userInquiryRepository;
    private final FileService fileService;
    private final UserRepository userRepository;

    /**
     * 1:1 문의 등록 (현재는 서버 로컬 저장, S3 사용시 내부 메서드 교체 필요)
     */
    @Transactional
    public void registerInquiry(InquiryCreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        String fileUrl = null;

        if (request.getAttachment() != null && !request.getAttachment().isEmpty()) {
            // 용량 및 특수문제 검증 수행
            fileService.validateFile(request.getAttachment());

            // 파일 서비스 호출 (지금은 로컬 저장, S3 사용시 메서드 교체 필요)
            fileUrl = fileService.uploadToLocal(request.getAttachment());
        }

        // DB 저장
        UserInquiry userInquiry = UserInquiry.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .inquiryType(request.getInquiryType())
                .fileUrl(fileUrl)
                .user(user)
                .build();

        userInquiryRepository.save(userInquiry);
    }

    /**
     * 1:1 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<InquirySummaryResponse> getMyInquiryList(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        // 1. DB에서 페이징된 엔티티 조회
        Page<UserInquiry> inquiries = userInquiryRepository.findAllByUser(user, pageable);

        // 2. 엔티티를 DTO로 변환하여 반환
        return inquiries.map(InquirySummaryResponse::from);
    }

    /**
     * 1:1 문의 내역 상세조회
     */
    @Transactional(readOnly = true)
    public InquiryDetailResponse getInquiry(Integer userInquiryId, String email) {
        UserInquiry inquiry = userInquiryRepository.findByUserInquiryId(userInquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("문의 내역을 찾을 수 없습니다."));

        return InquiryDetailResponse.from(inquiry);
    }
}
