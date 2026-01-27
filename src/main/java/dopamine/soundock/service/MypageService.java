package dopamine.soundock.service;

import dopamine.soundock.dto.request.CurrentPasswdRequest;
import dopamine.soundock.dto.request.UpdateInfoRequest;
import dopamine.soundock.dto.request.UpdatePasswdRequest;
import dopamine.soundock.dto.response.MyInfoResponse;
import dopamine.soundock.entity.Oauth;
import dopamine.soundock.dto.response.PaymentHistoryResponse;
import dopamine.soundock.dto.response.PopHistoryResponse;
import dopamine.soundock.entity.AccessTokenBlacklist;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopTarget;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.*;
import dopamine.soundock.repository.OauthRepository;
import dopamine.soundock.global.TokenProvider;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.AccessTokenBlacklistRepository;
import dopamine.soundock.repository.PopHistoryRepository;
import dopamine.soundock.repository.RefreshTokenRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cglib.core.Local;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MypageService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final OauthRepository oauthRepository;
    private final YouTubeAuthService youTubeAuthService;
    private final TokenProvider tokenProvider;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;
    private final PopHistoryRepository popHistoryRepository;

    // 내 정보 조회
    @Transactional(readOnly = true)
    public MyInfoResponse getMyInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자 입니다."));

        boolean isConnected = youTubeAuthService.validateAndCleanupOAuth(user);

        return MyInfoResponse
                .builder()
                .email(user.getEmail())
                .nickname(user.getNickname())
                .phoneNumber(user.getPhoneNumber())
                .isYoutubeConnected(isConnected)
                .build();
    }


    // 유저 정보 수정
    @Transactional
    public void updateUserInfo(UpdateInfoRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 닉네임 수정 할 시
        if (request.getNickname() != null
                && !request.getNickname().equals(user.getNickname())
                && !request.getNickname().isBlank()) {
            // 닉네임 중복 체크
            if (userRepository.existsByNickname(request.getNickname())) {
                throw new DuplicateNicknameException();
            }
            user.setNickname(request.getNickname());
        }

        // 휴대폰 번호 수정 할 시
        if (request.getPhoneNumber() != null
                && !request.getPhoneNumber().isBlank()
                && !request.getPhoneNumber().equals(user.getPhoneNumber())) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        // 저장
        userRepository.save(user);
    }

    // 비밀번호 검증
    @Transactional(readOnly = true)
    public void checkCurrentPassword(CurrentPasswdRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new CustomException("현재 비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST);
        }
    }

    // 비밀번호 수정
    @Transactional
    public void updateUserPasswd(UpdatePasswdRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 새 비밀번호가 기존 비밀번호와 같은지 확인
        if (passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException("현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        user.setPassword(encodedPassword);

        // 저장
        userRepository.save(user);
    }

    // 회원 탈퇴
    @Transactional
    public void deleteUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 이미 탈퇴한 사용자인지 확인
        if (user.isDeleted()) {
            throw new CustomException("이미 탈퇴한 사용자입니다.", HttpStatus.BAD_REQUEST);
        }

        // 닉네임 중복 제약 조건을 해제하기 위해 고유한 값으로 변경 (원래 닉네임 + _deleted + user.getId + 현재시간)
        String originalNickname = user.getNickname();
        String deletedNickname = originalNickname + "_deleted_" + user.getId() + "_" + System.currentTimeMillis();
        user.setNickname(deletedNickname);

        // 유저 상태 변경 (Soft Delete)
        user.setDeleted(true);
        user.setStatus(UserStatus.QUITTED);
        user.setDeletedAt(LocalDateTime.now());

        userRepository.save(user);

        // Refresh Token 무효화
        refreshTokenRepository.deleteByUserId(user.getId());
    }

    // 재화 구매(충전) 내역 조회
    public List<PaymentHistoryResponse> getPaymentHistory(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        String email = "linlin@gmail.com";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // popHistory 내역에서 사용자에 대한 정보 조회
        List<PopHistory> results = popHistoryRepository.findByUserOrderByCreatedDatetimeDesc(user);
        if (results.isEmpty()){
            throw new ResourceNotFoundException("구매 내역이 없습니다.");
        }

        // 엔티티 정보를 받을 response 배열 생성
        List<PaymentHistoryResponse> paymentHistoryResponses = new ArrayList<>();

        for (PopHistory popHistory : results){
            LocalDateTime expiredDatetime =
                    popHistory.getCreatedDatetime().plusYears(AppConstants.Time.POP_HISTORY_EXPIRATION_YEARS);
            // 구매 취소 여부
            boolean isCanceled = popHistory.getCanceledDatetime() != null;

            // popHistory 내역들 dto로 전환
            PaymentHistoryResponse response = new PaymentHistoryResponse(
                    popHistory.getCreatedDatetime(),
                    popHistory.getChangeAmount(),
                    popHistory.getPopTarget(),
                    popHistory.getActualAmount(),
                    expiredDatetime,
                    isCanceled
            );
            paymentHistoryResponses.add(response);
        }
        return paymentHistoryResponses;
    }

    // 재화 사용 내역 조회
    public List<PopHistoryResponse> getPopUsageHistory(){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        String email = "linlin@gmail.com";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // popHistory 내역에서 사용자에 대한 정보 조회
        // requested_at이 채워져있으면 사용한다고 요청이 들어온 상태
        // target이 DONATION, FEATURED BOARD인 경우만 보여주기 위한 리스트
        List<PopTarget> targets = List.of(PopTarget.DONATION, PopTarget.FEATURED_BOARD);
        List<PopHistory> results = popHistoryRepository.findByUserAndRequestedDatetimeIsNotNullAndPopTargetIn(user, targets);

        if (results.isEmpty()) {
            throw new ResourceNotFoundException("사용 내역이 없습니다.");
        }

        List<PopHistoryResponse> responses = new ArrayList<>();


        for (PopHistory popHistory : results) {
            PopHistoryResponse.RelatedInfo related = PopHistoryResponse.createRelatedInfo(popHistory);

            // 재화 사용 내역 popHistory dto로 전환
            // 사용일시, 사용수량, 사용내용(target), 사용대상(boardId, related_user)
            PopHistoryResponse popHistoryResponse = new PopHistoryResponse(
                    popHistory.getApprovedDatetime(),
                    popHistory.getChangeAmount(),
                    popHistory.getPopTarget(),
                    related
            );
            responses.add(popHistoryResponse);
        }
        return responses;
    }


}
