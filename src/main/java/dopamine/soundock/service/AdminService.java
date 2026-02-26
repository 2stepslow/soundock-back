package dopamine.soundock.service;


import dopamine.soundock.dto.TokenDto;
import dopamine.soundock.dto.request.AnnouncementCreateRequest;
import dopamine.soundock.dto.request.LoginRequest;
import dopamine.soundock.dto.response.AdminSettlementResponse;
import dopamine.soundock.dto.response.CancelRequestResponse;
import dopamine.soundock.entity.*;
import dopamine.soundock.enums.*;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.LoginFailedException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final PopHistoryRepository popHistoryRepository;
    private final AnnouncementRepository announcementRepository;
    private final AttachmentAfterCommitService attachmentAfterCommitService;
    private final AnnouncementAttachmentRepository announcementAttachmentRepository;
    private final S3Service s3Service;


    // 관리자 로그인
    @Transactional
    public TokenDto login(
            LoginRequest loginRequest
    ) {
        // 1. 존재 여부 확인 (탈퇴 시에도 Exception 발생)
        User user = userRepository.findByEmailAndIsDeletedFalse(loginRequest.getEmail())
                .orElseThrow(() -> new LoginFailedException("가입되지 않은 계정입니다."));

        if (user.isPasswordless()) {
            throw new LoginFailedException("패스워드리스 서비스를 사용 중입니다. 패스워드리스를 통해 로그인 해주세요");
        }

        // 2. 비밀번호 검증
        if(!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new LoginFailedException("비밀번호가 일치하지 않습니다.");
        }

        if(!user.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("접근 권한이 없습니다.");
        }

        // 3. 상태 검증
        if(!user.getStatus().equals(UserStatus.ACTIVE)) {
            throw new CustomException("사용할 수 없는 아이디 입니다. 관리자에게 문의해주세요.", HttpStatus.FORBIDDEN);
        }

        // 발급된 토큰이 있다면 삭제
        refreshTokenRepository.deleteByUserId(user.getId());
        // 로그인 성공 -> 토큰 발급
        String accessToken = tokenProvider.generateAccessToken(user.getEmail(), user.getId(), user.getRole().name());
        String refreshToken = tokenProvider.generateRefreshToken(user.getEmail());

        // Refresh Token 을 DB에 추가
        RefreshToken refresh = RefreshToken
                .builder()
                .token(refreshToken)
                .user(user)
                .expirationAt(LocalDateTime.now().plusSeconds(AppConstants.Time.REFRESH_TOKEN_VALIDITY_MS/1000))
                .build();

        refreshTokenRepository.save(refresh);

        return new TokenDto(accessToken, refreshToken);
    }

    // 후원 취소요청 리스트 조회
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(readOnly = true)
    public List<CancelRequestResponse> getCancelRequests() {

         String email = SecurityContextHolder.getContext().getAuthentication().getName();
         User admin = userRepository.findByEmail(email)
                 .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));


        // CANCEL_REQUEST 상태인 DONATION 내역만 조회
        List<PopHistory> cancelRequests = popHistoryRepository
                .findByPopStatusAndPopTargetOrderByRequestedDatetimeDesc(
                        PopStatus.CANCEL_REQUEST,
                        PopTarget.DONATION
                );

        if (cancelRequests.isEmpty()) {
            throw new ResourceNotFoundException("취소 요청 내역이 없습니다.");
        }

        return cancelRequests.stream()
                .map(CancelRequestResponse::from)
                .toList();
    }


    // 후원 취소 요청 승인
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void approveCancelDonation(String transactionId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));


        // transactionId로 DONATION과 RECEIVED 내역 조회
        List<PopHistory> histories = popHistoryRepository.findByTransactionId(transactionId);

        if (histories.isEmpty()) {
            throw new ResourceNotFoundException("해당 거래 내역을 찾을 수 없습니다.");
        }

        long donationCount = histories.stream()
                .filter(h -> h.getPopTarget() == PopTarget.DONATION)
                .count();

        long receivedCount = histories.stream()
                .filter(h -> h.getPopTarget() == PopTarget.RECEIVED)
                .count();

        if (donationCount != 1 || receivedCount != 1) {
            throw new CustomException("거래 내역이 올바르지 않습니다.", HttpStatus.CONFLICT);
        }


        // DONATION과 RECEIVED 구분
        PopHistory donatedHistory = histories.stream()
                .filter(h -> h.getPopTarget().equals(PopTarget.DONATION))
                .findFirst()
                .orElseThrow(() -> new CustomException("거래 내역이 올바르지 않습니다.", HttpStatus.NOT_FOUND
                ));

        PopHistory receivedHistory = histories.stream()
                .filter(h -> h.getPopTarget().equals(PopTarget.RECEIVED))
                .findFirst()
                .orElseThrow(() -> new CustomException("거래 내역이 올바르지 않습니다.", HttpStatus.NOT_FOUND
                ));

        // 상태 검증 - CANCEL_REQUEST 상태인지 확인
        if (!donatedHistory.getPopStatus().equals(PopStatus.CANCEL_REQUEST)) {
            throw new CustomException("취소 요청 상태가 아닙니다.", HttpStatus.CONFLICT);
        }

        if (!receivedHistory.getPopStatus().equals(PopStatus.CANCEL_REQUEST)) {
            throw new CustomException("취소 요청 상태가 아닙니다.", HttpStatus.CONFLICT);
        }

        User donator = donatedHistory.getUser();

        // 후원자에게 재화 복원 (절대값으로 변환하여 증가)
        int refundAmount = Math.abs(donatedHistory.getChangeAmount());
        int updatedRow = userRepository.increasePopBalance(donator.getEmail(), refundAmount);

        if (updatedRow == 0) {
            throw new CustomException("재화 복원에 실패했습니다. 사용자를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        // 양쪽 PopHistory 상태를 CANCELED로 변경 및 취소 일시 설정
        LocalDateTime now = LocalDateTime.now();
        donatedHistory.setPopStatus(PopStatus.CANCELED);
        donatedHistory.setCanceledDatetime(now);
        donatedHistory.setApprovedDatetime(now);


        receivedHistory.setPopStatus(PopStatus.CANCELED);
        receivedHistory.setCanceledDatetime(now);
        receivedHistory.setApprovedDatetime(now);

        popHistoryRepository.save(donatedHistory);
        popHistoryRepository.save(receivedHistory);

    }

    // 공지사항 작성
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public int createAnnouncement(
            AnnounceType announceType,
            AnnouncementCreateRequest createRequest,
            List<MultipartFile> files
    ) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        // 파일 개수 검증
        validateFileCounts(files);

        // 게시 시작일이 없으면 현재 시간으로 설정
        LocalDateTime startedAt = createRequest.getStartedAt();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }

        // 타입별 기존 priority=0 은 1로만 변경
        announcementRepository.bumpOnlyZeroToOne(announceType);

        // 공지사항 생성 - 새 글은 항상 priority=0
        Announcement announcement = Announcement.builder()
                .announceType(announceType)
                .title(createRequest.getTitle())
                .content(createRequest.getContent())
                .linkUrl(createRequest.getLinkUrl())
                .priority(0)
                .isActive(createRequest.getIsActive())
                .startedAt(startedAt)
                .endedAt(createRequest.getEndedAt())
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);

        // 첨부파일 업로드/저장은 afterCommit 이후에만 수행
        if (files != null && !files.isEmpty()) {
            final Integer announceId = savedAnnouncement.getAnnounceId();
            final List<MultipartFile> filesCopy = List.copyOf(files);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    attachmentAfterCommitService.uploadAndSaveAnnouncementAttachments(announceId, filesCopy);
                }
            });
        }

        return savedAnnouncement.getAnnounceId();
    }

    // 공지사항 수정
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void updateAnnouncement(
            Integer announceId,
            AnnouncementCreateRequest updateRequest,
            List<MultipartFile> newFiles,
            List<Integer> deleteAttachmentIds
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        Announcement announcement = announcementRepository.findById(announceId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));

        // 들어온 값만 변경 (priority는 아래에서 따로 처리)
        if (updateRequest != null) {
            if (updateRequest.getTitle() != null) announcement.setTitle(updateRequest.getTitle());
            if (updateRequest.getContent() != null) announcement.setContent(updateRequest.getContent());
            if (updateRequest.getLinkUrl() != null) announcement.setLinkUrl(updateRequest.getLinkUrl());
            if (updateRequest.getIsActive() != null) announcement.setIsActive(updateRequest.getIsActive());
            if (updateRequest.getStartedAt() != null) announcement.setStartedAt(updateRequest.getStartedAt());
            if (updateRequest.getEndedAt() != null) announcement.setEndedAt(updateRequest.getEndedAt());
        }

        // priority 처리, 타입별 priority=0은 항상 1개 유지
        if (updateRequest != null && updateRequest.getPriority() != null) {
            Integer newPriority = updateRequest.getPriority();

            // 0으로 설정하려고 할 때만 bump 로직 실행
            if (newPriority == 0 && (announcement.getPriority() == null || announcement.getPriority() != 0)) {
                announcementRepository.bumpOnlyZeroToOneExceptSelf(
                        announcement.getAnnounceType(),
                        announcement.getAnnounceId()
                );
            }

            announcement.setPriority(newPriority);
        }

        // 첨부 삭제 (S3 + DB)
        if (deleteAttachmentIds != null && !deleteAttachmentIds.isEmpty()) {
            List<AnnouncementAttachment> toDelete = announcementAttachmentRepository.findAllById(deleteAttachmentIds);
            for (AnnouncementAttachment attachment : toDelete) {
                if (!attachment.getAnnouncement().getAnnounceId().equals(announceId)) {
                    throw new AccessDeniedException("다른 공지사항의 첨부파일은 삭제할 수 없습니다.");
                }
                s3Service.deleteFile(attachment.getFileKey());
                announcementAttachmentRepository.delete(attachment);
            }
        }

        // 신규 파일 afterCommit 업로드
        if (newFiles != null && !newFiles.isEmpty()) {
            validateFileCounts(newFiles);

            final Integer id = announcement.getAnnounceId();
            final List<MultipartFile> filesCopy = List.copyOf(newFiles);

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    attachmentAfterCommitService.uploadAndSaveAnnouncementAttachments(id, filesCopy);
                }
            });
        }

        announcementRepository.save(announcement);
    }


    // 공지사항 삭제 - isactive가 있으므로 하드 딜리트 작성
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void deleteAnnouncement(Integer announceId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        Announcement announcement = announcementRepository.findById(announceId)
                .orElseThrow(() -> new ResourceNotFoundException("공지사항을 찾을 수 없습니다."));

        // s3 파일 삭제
        if (announcement.getAttachments() != null && !announcement.getAttachments().isEmpty()) {
            for (AnnouncementAttachment attachment : announcement.getAttachments()) {
                if (attachment.getFileKey() != null && !attachment.getFileKey().isBlank()) {
                    s3Service.deleteFile(attachment.getFileKey());
                }
            }
        }

        announcementRepository.delete(announcement);
    }


    // 파일 개수 검증 메서드
    private void validateFileCounts(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) return;

        long imageCount = files.stream()
                .filter(f -> f.getContentType() != null && f.getContentType().startsWith("image/"))
                .count();

        long fileCount = files.stream()
                .filter(f -> f.getContentType() != null && !f.getContentType().startsWith("image/"))
                .count();

        if (imageCount > 5) {
            throw new IllegalArgumentException("이미지는 최대 5장까지 업로드 가능합니다.");
        }
        if (fileCount > 5) {
            throw new IllegalArgumentException("파일은 최대 5개까지 업로드 가능합니다.");
        }
    }

    // 정산 요청 내역 조회
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public List<AdminSettlementResponse> getAdminSettlement(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        // 사용자가 신청한 정산 내역들 표시 (정산 요청 + 정산 완료)
        List<PopHistory> popHistories = popHistoryRepository.findByPopStatusIn(
                List.of(PopStatus.SETTLEMENT_REQUEST, PopStatus.SETTLEMENT_COMPLETED));

        if (popHistories.isEmpty()){
            throw new ResourceNotFoundException("정산 요청 내역이 없습니다.");
        }

        List<AdminSettlementResponse> adminSettlementResponses = new ArrayList<>();
        for (PopHistory popHistory : popHistories){
            AdminSettlementResponse response = AdminSettlementResponse.builder()
                    .popHistoryId(popHistory.getPopHistoryId())
                    .userId(popHistory.getUser().getId())
                    .nickName(popHistory.getUser().getNickname())
                    .changeAmount(popHistory.getChangeAmount())
                    .requestedDatetime(popHistory.getRequestedDatetime())
                    .approvedDatetime(popHistory.getApprovedDatetime())
                    .popStatus(popHistory.getPopStatus())
                    .build();

            adminSettlementResponses.add(response);
        }
        return adminSettlementResponses;
    }

    // 정산 승인 처리
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void approveSettlement(Integer popHistoryId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        // 정산 요청 내역 조회
        PopHistory popHistory = popHistoryRepository.findById(popHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 정산 내역을 찾을 수 없습니다."));

        // 상태 검증 - SETTLEMENT_REQUEST 상태인지 확인
        if (!popHistory.getPopStatus().equals(PopStatus.SETTLEMENT_REQUEST)) {
            throw new CustomException("정산 요청 상태가 아닙니다. 현재 상태: " + popHistory.getPopStatus(), HttpStatus.CONFLICT);
        }

        // 정산 승인 처리
        LocalDateTime now = LocalDateTime.now();
        popHistory.setPopStatus(PopStatus.SETTLEMENT_COMPLETED);
        popHistory.setApprovedDatetime(now);

        popHistoryRepository.save(popHistory);
    }

    // 정산 거절 처리
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void rejectSettlement(Integer popHistoryId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.ErrorMessage.USER_NOT_FOUND));

        // 정산 요청 내역 조회
        PopHistory popHistory = popHistoryRepository.findById(popHistoryId)
                .orElseThrow(() -> new ResourceNotFoundException("해당 정산 내역을 찾을 수 없습니다."));

        // 상태 검증 - SETTLEMENT_REQUEST 상태인지 확인
        if (!popHistory.getPopStatus().equals(PopStatus.SETTLEMENT_REQUEST)) {
            throw new CustomException("정산 요청 상태가 아닙니다. 현재 상태: " + popHistory.getPopStatus(), HttpStatus.CONFLICT);
        }

        // 정산 거절 처리 - 상태를 COMPLETED로 되돌려서 사용자가 다시 정산 요청 가능하도록
        popHistory.setPopStatus(PopStatus.COMPLETED);
        popHistory.setRequestedDatetime(null);

        popHistoryRepository.save(popHistory);
    }
}
