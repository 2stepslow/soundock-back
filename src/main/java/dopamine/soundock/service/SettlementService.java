package dopamine.soundock.service;

import dopamine.soundock.dto.request.RegisterSettlementRequest;
import dopamine.soundock.dto.response.AvailableSettlementResponse;
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

        // 로그인한 유저와 정산 정보의 유저 이메일과 일치하는지 검증
        if (!settlementRequest.getEmail().equals(user.getEmail())){
            throw new IllegalArgumentException("회원 email과 일치하지 않습니다. 수혜자 본인에 대해서만 정산 정보 등록이 가능합니다.");
        }
        // 회원 가입 시 입력한 이름과 일치하는지 검증
        if (!settlementRequest.getName().equals(user.getName())){
            throw new IllegalArgumentException("회원 이름과 일치하지 않습니다. 수혜자 본인에 대해서만 정산 정보 등록이 가능합니다.");
        }
        // 회원 가입 시 입력한 전화번호와 일치하는지 검증
        if (!settlementRequest.getPhoneNumber().equals(user.getPhoneNumber())){
            throw new IllegalArgumentException("회원 정보에 등록된 전화번호와 일치하지 않습니다.");
        }
    }

    // 정산 신청
    @Transactional
    public AvailableSettlementResponse requestSettlement(){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // popStatus = COMPLETED, target = RECEIVED인 정산 요청, 승인 기록이 없는 popHistory 내역 조회
        // 후원 받은 날짜(createdDatetime)로부터 3일이 지나야 정산 신청 가능
        LocalDateTime availableDay = LocalDateTime.now().minusDays(AppConstants.Time.AVAILABLE_REQUEST_SETTLEMENT_DAYS);
        List<PopHistory> availableSettlements = popHistoryRepository.findAvailableSettlementForUpdate(
                user.getId(), PopTarget.RECEIVED, PopStatus.COMPLETED, availableDay);

        // popHistory 내역이 없을 경우 예외
        if (availableSettlements.isEmpty()){
            throw new ResourceNotFoundException("현재 정산 가능한 내역이 없습니다.");
        }

        int totalSettleAmount = 0;
        // 정산하는 popHistory의 Id 담을 배열 생성
        List<Integer> settlementsIds = new ArrayList<>();

        LocalDateTime now = LocalDateTime.now();
        // 정산 신청한 popHistory 내역들 상태 업데이트
        for (PopHistory popHistory : availableSettlements){
            totalSettleAmount = totalSettleAmount + popHistory.getChangeAmount();
            settlementsIds.add(popHistory.getPopHistoryId());
        }
        // 한 번에 popHistory의 상태 업데이트
        popHistoryRepository.updateSettlementPopHistory(PopStatus.SETTLEMENT_REQUEST, now, settlementsIds);

        log.info("총 정산 요청된 건수는 : {}건, 총 정산 금액은 : {} 원 입니다.", availableSettlements.size() , totalSettleAmount);
        return AvailableSettlementResponse.builder()
                .totalCount(availableSettlements.size())
                .totalAmount(totalSettleAmount)
                .popHistoryResponses(new ArrayList<>())
                .build();
    }

    // 정산 가능 내역 조회
    @Transactional(readOnly = true)
    public AvailableSettlementResponse getListAvailableSettlement(){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 정산 가능한 popHistory 조회
        LocalDateTime availableDay = LocalDateTime.now().minusDays(AppConstants.Time.AVAILABLE_REQUEST_SETTLEMENT_DAYS);
        List<PopHistory> availableSettlements = popHistoryRepository.findAvailableSettlement(
                user.getId(), PopTarget.RECEIVED, PopStatus.COMPLETED, availableDay);

        // 정산 가능 내역이 없을 때 빈 배열 반환
        if (availableSettlements.isEmpty()){
            return AvailableSettlementResponse.builder()
                    .totalAmount(0)
                    .totalCount(0)
                    .popHistoryResponses(new ArrayList<>())
                    .build();
        }
        int totalAmount = 0;
        for (PopHistory popHistory : availableSettlements){
            totalAmount += popHistory.getChangeAmount();
        }

        // 정산 가능한 내역 pop dto 전환
        List<PopHistoryResponse> responses = new ArrayList<>();
        for (PopHistory popHistory : availableSettlements){
            PopHistoryResponse response = PopHistoryResponse.fromSettlement(popHistory);
            responses.add(response);
        }

        return AvailableSettlementResponse.builder()
                .totalAmount(totalAmount)
                .totalCount(availableSettlements.size())
                .popHistoryResponses(responses)
                .build();
    }

    // 정산 내역 조회
    @Transactional(readOnly = true)
    public List<PopHistoryResponse> getListSettlement(){
        // 로그인한 유저 확인
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다."));

        // 정산 요청 중, 정산 완료 기록 표시
        // popStatus = 'SETTLEMENT REQUEST' or 'SETTLEMENT COMPLETED'
        // canceledDatetime IS NULL
        List<PopStatus> statuses = List.of(PopStatus.SETTLEMENT_REQUEST, PopStatus.SETTLEMENT_COMPLETED);
        List<PopHistory> settlementPop = popHistoryRepository.findMySettlementList(user.getId(), statuses);

        if (settlementPop.isEmpty()){
            return new ArrayList<>();
        }

        List<PopHistoryResponse> settlementResponse = new ArrayList<>();

        for (PopHistory popHistory : settlementPop){
            PopHistoryResponse popHistoryResponse = PopHistoryResponse.fromSettlement(popHistory);
            settlementResponse.add(popHistoryResponse);
        }
        return settlementResponse;
    }
}
