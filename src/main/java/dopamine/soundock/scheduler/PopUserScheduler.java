package dopamine.soundock.scheduler;

import dopamine.soundock.entity.PopHistory;
import dopamine.soundock.enums.PopStatus;
import dopamine.soundock.repository.PopHistoryRepository;
import dopamine.soundock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopUserScheduler {

    private final PopHistoryRepository popHistoryRepository;
    private final UserRepository userRepository;

    @Transactional
    @Scheduled(cron = "0 0 * * * *") // 매 1시간마다
    public void updateUserPopStatus(){
        LocalDateTime now = LocalDateTime.now();

        try {
            // DONATION 처리 (현재 시간 - 3일 이전의 데이터), DB가 쿼리문이 실행되고 실제 변경된 데이터가 몇건인지 계산해서 반환해줌
            int donationCount = popHistoryRepository.updateDonation(now, now.minusDays(3));

            // RECEIVED 처리 (현재 시간 - 3일 이전의 데이터), 상태변경 + 포인트 증가
            List<PopHistory> receivedList = popHistoryRepository.findPendingReceived(now.minusDays(3));

            for (PopHistory popHistory : receivedList) {
                // 1. 포인트 변경 (users 테이블의 popbalance)
                Integer userId = popHistory.getUser().getId();
                Integer amount = popHistory.getChangeAmount();
                userRepository.incrementUserPoint(userId, amount);

                // 2. 상태 변경 (pophistory 테이블의 popstatus)
                popHistory.setPopStatus(PopStatus.COMPLETED);
                popHistoryRepository.save(popHistory);
            }

            // 로그 대박 터지는거 방지하기 위해 실제로 처리가 됐을때만 로그 찍기
            if (donationCount > 0 || !receivedList.isEmpty()) {
                log.info("[스케쥴러 결과] DONATION: {}건, RECEIVED 대상: {}건",
                        donationCount, receivedList.size());
            }
        } catch (Exception e) {
            log.error("PopStatus 정리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }
}
