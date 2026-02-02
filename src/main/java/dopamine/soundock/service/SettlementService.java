package dopamine.soundock.service;

import dopamine.soundock.dto.request.RegisterSettlementRequest;
import dopamine.soundock.dto.response.PopHistoryResponse;
import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.entity.User;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.enums.PopTarget;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class SettlementService {
    private final PopHistoryRepository popHistoryRepository;
    private final UserRepository userRepository;

    // 정산 정보 등록
    public void registerSettlementInfo(RegisterSettlementRequest settlementRequest){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 로그인한 유저와 정산 정보의 유저 아이디와 일치하는지 검증
        if (!settlementRequest.getUserId().equals(user.getId())){
            throw new IllegalArgumentException("수혜자 본인에 대해서만 정산 정보 등록이 가능합니다.");
        }
        // 회원 가입 시 입력한 전화번호와 일치하는지 검증
        if (settlementRequest.getPhoneNumber().equals(user.getPhoneNumber())){
            throw new IllegalArgumentException("회원 정보에 등록된 전화번호와 일치하지 않습니다.");
        }
    }

    // 정산 신청
    @Transactional
    public void requestSettlement(){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // popStatus = COMPLETED, target = RECEIVED인 정산 요청, 승인 기록이 없는 popHistory 내역 조회
        // 후원 받은 날짜(createdDatetime)로부터 3일이 지나야 정산 신청 가능
        LocalDateTime availableDay = LocalDateTime.now().minusDays(AppConstants.Time.AVAILABLE_REQUEST_SETTLEMENT_DAYS);
        List<PopHistory> availableSettlement = popHistoryRepository.findAvailableSettlement(
                user.getId(), PopTarget.RECEIVED, PopStatus.COMPLETED ,availableDay);

        // popHistory 내역이 없을 경우 예외
        if (availableSettlement.isEmpty()){
            throw new ResourceNotFoundException("현재 정산 가능한 내역이 없습니다.");
        }
        // 정산 신청한 popHistory 내역들 상태 업데이트
        int totalSettleAmount = 0;
        for (PopHistory popHistory : availableSettlement){
            popHistory.requestSettlementPop();
            totalSettleAmount = totalSettleAmount + popHistory.getChangeAmount();
        }
        log.info("총 정산 요청된 건수는 : {}건, 총 정산 금액은 : {} 원 입니다.", availableSettlement.size() , totalSettleAmount);
    }

    // 정산 내역 조회
    public List<PopHistoryResponse> getListSettlement(){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 정산 요청 중, 정산 완료 기록 표시
        // popStatus = 'SETTLEMENT REQUEST' or 'SETTLEMENT COMPLETED'
        // approvedDatetime IS NOT NULL
        List<PopHistory> settlementPop = popHistoryRepository.findMySettlementList(user.getId());

        if (settlementPop.isEmpty()){
            throw new ResourceNotFoundException("정산을 요청하거나 완료된 정산 내역이 없습니다.");
        }

        List<PopHistoryResponse> settlementResponse = new ArrayList<>();

        for (PopHistory popHistory : settlementPop){
            PopHistoryResponse popHistoryResponse = PopHistoryResponse.builder()
                    .userId(popHistory.getUser().getId())
                    .popHistoryId(popHistory.getPopHistoryId())
                    .popStatus(popHistory.getPopStatus())
                    .popTarget(popHistory.getPopTarget())
                    .changeAmount(popHistory.getChangeAmount())
                    .requestedDatetime(popHistory.getRequestedDatetime())
                    .approvedDatetime(popHistory.getApprovedDatetime())
                    .build();

            settlementResponse.add(popHistoryResponse);
        }
        return settlementResponse;
    }
}
