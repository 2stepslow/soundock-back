package dopamine.soundock.service;


import dopamine.soundock.dto.TokenDto;
import dopamine.soundock.dto.request.AnnouncementCreateRequest;
import dopamine.soundock.dto.request.LoginRequest;
import dopamine.soundock.dto.response.CancelRequestResponse;
import dopamine.soundock.dto.response.FileUploadResponse;
import dopamine.soundock.entity.*;
import dopamine.soundock.enums.*;
import dopamine.soundock.exceptions.CustomException;
import dopamine.soundock.exceptions.LoginFailedException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.exceptions.UnauthorizedException;
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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final PopHistoryRepository popHistoryRepository;
    private final AnnouncementRepository announcementRepository;
    private final S3Service s3Service;
    private final AnnouncementAttachmentRepository announcementAttachmentRepository;


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
                 .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));


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
                .collect(Collectors.toList());
    }


    // 후원 취소 요청 승인
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void approveCancelDonation(String transactionId) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));


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

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public int createAnnouncement(
            AnnounceType announceType,
            AnnouncementCreateRequest createRequest,
            List<MultipartFile> files
    ) throws IOException {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        if (!admin.getRole().equals(UserRole.ADMIN)) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }

        // 파일 개수 검증 (이미지 5장, 파일 5개)
        if (files != null && !files.isEmpty()) {
            long imageCount = files.stream()
                    .filter(file -> file.getContentType() != null && file.getContentType().startsWith("image/"))
                    .count();
            long fileCount = files.stream()
                    .filter(file -> file.getContentType() != null && !file.getContentType().startsWith("image/"))
                    .count();

            if (imageCount > 5) {
                throw new IllegalArgumentException("이미지는 최대 5장까지 업로드 가능합니다.");
            }
            if (fileCount > 5) {
                throw new IllegalArgumentException("파일은 최대 5개까지 업로드 가능합니다.");
            }
        }

        // 게시 시작일이 없으면 현재 시간으로 설정
        LocalDateTime startedAt = createRequest.getStartedAt();
        if (startedAt == null) {
            startedAt = LocalDateTime.now();
        }

        // 공지사항 생성
        Announcement announcement = Announcement.builder()
                .announceType(announceType)
                .title(createRequest.getTitle())
                .content(createRequest.getContent())
                .linkUrl(createRequest.getLinkUrl())
                .priority(createRequest.getPriority())
                .isActive(createRequest.getIsActive())
                .startedAt(startedAt)
                .endedAt(createRequest.getEndedAt())
                .build();

        Announcement savedAnnouncement = announcementRepository.save(announcement);

        // 첨부파일 업로드 및 저장
        if (files != null && !files.isEmpty()) {
                List<FileUploadResponse> uploadedFiles = s3Service.uploadFiles(files);

            // 파일 순서대로 DB에 저장
            for (int i = 0; i < uploadedFiles.size(); i++) {
                FileUploadResponse fileResponse = uploadedFiles.get(i);
                FileType fileType = fileResponse.getIsImage() ? FileType.IMAGE : FileType.FILE;

                AnnouncementAttachment attachment = AnnouncementAttachment.builder()
                        .announcement(savedAnnouncement)
                        .fileUrl(fileResponse.getFileUrl())
                        .fileKey(fileResponse.getFileKey())
                        .fileType(fileType)
                        .originalFilename(fileResponse.getOriginalFilename())
                        .build();

                announcementAttachmentRepository.save(attachment);
            }
        }

        return savedAnnouncement.getAnnounceId();
    }



}
