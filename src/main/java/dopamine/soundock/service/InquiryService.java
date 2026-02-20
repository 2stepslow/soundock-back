package dopamine.soundock.service;

import dopamine.soundock.dto.request.AdminCommentRequest;
import dopamine.soundock.dto.request.InquiryCreateRequest;
import dopamine.soundock.dto.response.InquiryDetailResponse;
import dopamine.soundock.dto.response.InquirySummaryResponse;
import dopamine.soundock.entity.User;
import dopamine.soundock.entity.UserInquiry;
import dopamine.soundock.enums.CommentStatus;
import dopamine.soundock.enums.InquiryStatus;
import dopamine.soundock.enums.UserRole;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.repository.UserInquiryRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {
    private final UserInquiryRepository userInquiryRepository;
    private final UserRepository userRepository;

    /**
     * 1:1 문의 등록
     */
    @Transactional
    public void registerInquiry(InquiryCreateRequest request, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));


        UserInquiry userInquiry = UserInquiry.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .inquiryType(request.getInquiryType())
                // 프론트에서 주는 URL + Key
                .fileUrl(request.getFileUrl())
                .fileKey(request.getFileKey())
                .isImage(request.getIsImage())
                .user(user)
                .build();

        userInquiryRepository.save(userInquiry);

    }

    /**
     * 1:1 문의 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<InquirySummaryResponse> getMyInquiryList(String email, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("유저를 찾을 수 없습니다."));

        Page<UserInquiry> inquiries;

        // 1-1. 시작일과 종료일이 모두 파라미터로 넘어온 경우 기간 검색 수행
        if (start != null && end != null) {
            inquiries = userInquiryRepository.findAllByUserAndCreatedAtBetween(user, start, end, pageable);
        } else {
            // 1-2. 날짜가 없으면 전체 내역 조회
            inquiries = userInquiryRepository.findAllByUser(user, pageable);
        }


        // 2. 엔티티를 DTO로 변환하여 반환
        return inquiries.map(InquirySummaryResponse::from);
    }

    /**
     * 1:1 문의 내역 상세조회
     */
    @Transactional(readOnly = true)
    public InquiryDetailResponse getInquiry(Integer userInquiryId, String email) {
        UserInquiry inquiry = userInquiryRepository.findByUserInquiryIdAndUser_Email(userInquiryId, email)
                .orElseThrow(() -> new ResourceNotFoundException("문의 내역을 찾을 수 없습니다."));

        return InquiryDetailResponse.from(inquiry);
    }

    /**
     * 관리자 1:1 문의 답변 메서드
     */
    @Transactional
    public void registerAdminComment(Integer inquiryId, AdminCommentRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException("유저를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));

        if (admin.getRole() != UserRole.ADMIN) {
            throw new CustomException("사용할 수 없는 기능입니다.", HttpStatus.FORBIDDEN);
        }

        UserInquiry inquiry = userInquiryRepository.findByUserInquiryId(inquiryId)
                .orElseThrow(() -> new ResourceNotFoundException("문의 내역을 찾을 수 없습니다."));

        if (inquiry.getCommentStatus() == CommentStatus.COMPLETED) {
            throw new CustomException("이미 답변이 완료된 문의 입니다.", HttpStatus.BAD_REQUEST);
        }

        inquiry.answerInquiry(admin, request.getAdminComment());

        userInquiryRepository.save(inquiry);
    }
}
