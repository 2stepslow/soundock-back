package dopamine.soundock.scheduler;

import dopamine.soundock.repository.PopHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PopStatusScheduler {

    private final PopHistoryRepository popHistoryRepository;

    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매 1분마다
    public void updatePopStatus(){
        LocalDateTime now = LocalDateTime.now();

        try {
            // DONATION 처리 (현재 시간 - 3일 이전의 데이터), DB가 쿼리문이 실행되고 실제 변경된 데이터가 몇건인지 계산해서 반환해줌
            int donationCount = popHistoryRepository.updateDonationStatus(now.minusDays(3));

            // FEATURED_BOARD 처리 (현재 시간 - 10분 이전의 데이터), DB가 쿼리문이 실행되고 실제 변경된 데이터가 몇건인지 계산해서 반환해줌
            int boardCount = popHistoryRepository.updateBoardStatus(now.minusMinutes(10));

            // 로그 대박 터지는거 방지하기 위해 실제로 처리가 됐을때만 로그 찍기
            if (donationCount > 0 || boardCount > 0) {
                log.info("pop 상태 처리 완료: DONATION: {}건, FEATURED_BOARD: {}건", donationCount, boardCount);
            }
        } catch (Exception e) {
            log.error("PopStatus 정리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }
}
