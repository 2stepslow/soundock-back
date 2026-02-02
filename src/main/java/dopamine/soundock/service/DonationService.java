package dopamine.soundock.service;

import dopamine.soundock.dto.request.DonationRequest;
import dopamine.soundock.dto.response.PopHistoryResponse;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
import dopamine.soundock.enums.UserStatus;
import dopamine.soundock.exceptions.InvalidCancelDonationException;
import dopamine.soundock.exceptions.ResourceNotFoundException;
import dopamine.soundock.global.constants.AppConstants;
import dopamine.soundock.repository.PopHistoryRepository;
import dopamine.soundock.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DonationService {
    private final UserRepository userRepository;
    private final PopHistoryRepository popHistoryRepository;

    // 후원 하기
    @Transactional
    public void donate(Integer targetUserId, DonationRequest donationRequest){
        // 사용자 로그인 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // targetUserId와 일치하는 유저 존재 확인
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("후원하려는 유저 정보를 찾을 수 없습니다."));

        // targetUser가 탈퇴한 회원인지 확인
        if(targetUser.isDeleted() || targetUser.getStatus().equals(UserStatus.QUITTED)){
            throw new IllegalArgumentException("탈퇴한 유저입니다. 탈퇴 유저에게 후원할 수 없습니다.");
        }

        // targetUserId와 후원자가 일치하는지 확인
        if (targetUser.getId().equals(user.getId())){
            throw new IllegalArgumentException("본인에게는 후원할 수 없습니다.");
        }

        // 내가 가진 재화 수량 확인
        // 내가 가진 재화보다 changeAmount 가 크면 안됨. 크면 예외 던져야지
        if (user.getPopBalance() == null || user.getPopBalance() < donationRequest.getChangeAmount()){
            throw new IllegalArgumentException("후원 금액보다 재화 보유량이 부족합니다. 재화 충전 페이지로 이동합니다.");
        }

        // 현재 후원하려는 유저의 재화 차감
        int updatedRow = userRepository.decreasePopBalance(user.getEmail(), donationRequest.getChangeAmount());
        if (updatedRow == 0){
            throw new IllegalArgumentException("차감할 재화가 없습니다. 재화가 부족하거나 이미 처리된 요청입니다.");
        }

        // 수혜자-후원자 한 쌍 확인용 UUID 생성
        String transactionId = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();

        // 내 popHistory 내역 생성
        PopHistory donatedPopHistory = PopHistory.builder()
                .transactionId(transactionId)
                .changeAmount(-donationRequest.getChangeAmount())
                .popStatus(PopStatus.COMPLETED)
                .createdDatetime(now)
                .requestedDatetime(now)
                .popTarget(PopTarget.DONATION)
                .relatedUser(targetUser)
                .user(user)
                .build();
        popHistoryRepository.save(donatedPopHistory);

        // 후원 대상 popHistory 내역 생성
        PopHistory receivedPopHistory = PopHistory.builder()
                .transactionId(transactionId)
                .changeAmount(donationRequest.getChangeAmount())
                .popStatus(PopStatus.COMPLETED)
                .createdDatetime(now)
                .popTarget(PopTarget.RECEIVED)
                .relatedUser(user)
                .user(targetUser)
                .build();
        popHistoryRepository.save(receivedPopHistory);
    }

    // 후원 취소 요청 하기
    @Transactional
    public void cancelDonation(Integer userId, DonationRequest donationCancelRequest){
        // 사용자 로그인 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 현재 로그인한 사용자와 경로에 받은 사용자 정보 검증
        if (!user.getId().equals(userId)){
            throw new IllegalArgumentException("현재 사용자 정보와 후원 취소 요청자의 정보가 다릅니다.");
        }

        // 후원한 내역 검증
        // popStatus = COMPLETED, popTarget = donation, created_at이 요청일 기준 3일 이내,
        // canceled_at = null인 후원 내역의 user_id가 일치해야함
        PopHistory donatedPopHistory =
                popHistoryRepository.findById(donationCancelRequest.getPopHistoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("취소하려는 후원 내역이 존재하지 않습니다."));

        // 후원자의 id가 일치하는지 검증
        if (!donatedPopHistory.getUser().getId().equals(user.getId())){
            throw new IllegalArgumentException("현재 사용자의 후원 내역과 일치하지 않습니다.");
        }

        // PopTarget이 DONATION인지 검증
        if (!donatedPopHistory.getPopTarget().equals(PopTarget.DONATION)){
            throw new IllegalArgumentException("타 유저에게 후원한 내역이 아닙니다.");
        }

        // 취소 요청 중이거나 취소된 내역인지 검증
        if (donatedPopHistory.getPopStatus().equals(PopStatus.CANCEL_REQUEST) ||
                donatedPopHistory.getPopStatus().equals(PopStatus.CANCELED)){
            throw new IllegalArgumentException("이미 취소 요청 중이거나 취소한 후원 내역입니다.");
        }
        if (donatedPopHistory.getCanceledDatetime() != null){
            throw new IllegalArgumentException("이미 취소된 내역입니다.");
        }

        // 후원 취소일 검증
        LocalDateTime availableCancel =
                LocalDateTime.now().minusDays(AppConstants.Time.AVAILABLE_REQUEST_CANCEL_DAYS);

        if (donatedPopHistory.getRequestedDatetime().isBefore(availableCancel)){
            throw new InvalidCancelDonationException("취소 요청 가능 기간(3일)이 지났습니다.");
        }

        // 찾은 후원 내역 상태 업데이트
        donatedPopHistory.requestCancelPop();

        // donatedPopHistory의 transactionId와 PopTarget이 RECEIVED인 receivedPopHistory를 찾음
        PopHistory receivedPopHistory = popHistoryRepository.findByTransactionIdAndPopTarget(donatedPopHistory.getTransactionId(), PopTarget.RECEIVED)
                .orElseThrow(() -> new IllegalArgumentException("후원자 내역과 수혜자 내역이 일치하지 않습니다."));

        if (receivedPopHistory.getRequestedDatetime() != null){
            throw new IllegalArgumentException("정산 처리 중인 내역은 취소할 수 없습니다.");
        }

        // 수혜자의 popHistory 기록을 cancel requested 로 업데이트
        receivedPopHistory.requestCancelPop();
    }

    // 후원 내역 조회(후원자)
    public List<PopHistoryResponse> getDonatedResult(Integer userId){
        // 사용자 로그인 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 조회하는 후원자 id와 로그인한 유저가 일치하는지 확인
        if (!user.getId().equals(userId)){
            throw new IllegalArgumentException("조회 시도하는 사용자의 정보와 로그인 정보가 일치하지 않습니다.");
        }

        // 로그인한 사람 후원 내역 조회. 없다면 예외 던짐
        List<PopHistory> donatedPop = popHistoryRepository.findByUserAndCreatedDatetimeIsNotNullAndPopTargetOrderByCreatedDatetimeDesc(user, PopTarget.DONATION);

        if (donatedPop.isEmpty()){
            throw new ResourceNotFoundException("후원 내역이 없습니다.");
        }

        // 후원 내역 dto로 전환
        List<PopHistoryResponse> donatedPopResponse = new ArrayList<>();

        for (PopHistory popHistory : donatedPop){
            PopHistoryResponse.RelatedInfo related = PopHistoryResponse.createRelatedInfo(popHistory);

            PopHistoryResponse response = PopHistoryResponse.builder()
                    .userId(popHistory.getUser().getId())
                    .popHistoryId(popHistory.getPopHistoryId())
                    .createdDatetime(popHistory.getCreatedDatetime())
                    .requestedDatetime(popHistory.getRequestedDatetime())
                    .approvedDatetime(popHistory.getApprovedDatetime())
                    .cancelDatetime(popHistory.getCanceledDatetime())
                    .changeAmount(popHistory.getChangeAmount())
                    .popTarget(popHistory.getPopTarget())
                    .related(related)
                    .build();
            donatedPopResponse.add(response);
        }
        return donatedPopResponse;
    }

    // 후원 내역 조회(수혜자)
    public List<PopHistoryResponse> getReceivedResult(Integer userId) {
        // 사용자 로그인 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 조회하는 후원자 id와 로그인한 유저가 일치하는지 확인
        if (!user.getId().equals(userId)){
            throw new IllegalArgumentException("조회 시도하는 사용자의 정보와 로그인 정보가 일치하지 않습니다.");
        }

        // 로그인한 사용자의 후원 받은 내역 조회. 없다면 예외 던짐
        List<PopHistory> receivedPop = popHistoryRepository.findByUserAndCreatedDatetimeIsNotNullAndPopTargetOrderByCreatedDatetimeDesc(user, PopTarget.RECEIVED);

        if (receivedPop.isEmpty()){
            throw new ResourceNotFoundException("후원 받은 내역이 없습니다.");
        }

        // popHistory 내역 dto로 전환
        List<PopHistoryResponse> receivedPopResponse = new ArrayList<>();

        for (PopHistory popHistory : receivedPop){
            PopHistoryResponse.RelatedInfo related = PopHistoryResponse.createRelatedInfo(popHistory);

            PopHistoryResponse response = PopHistoryResponse.builder()
                    .userId(popHistory.getUser().getId())
                    .popHistoryId(popHistory.getPopHistoryId())
                    .createdDatetime(popHistory.getCreatedDatetime())
                    .requestedDatetime(popHistory.getRequestedDatetime())
                    .approvedDatetime(popHistory.getApprovedDatetime())
                    .cancelDatetime(popHistory.getCanceledDatetime())
                    .changeAmount(popHistory.getChangeAmount())
                    .popTarget(popHistory.getPopTarget())
                    .related(related)
                    .build();

            receivedPopResponse.add(response);
        }
        return receivedPopResponse;
    }
}