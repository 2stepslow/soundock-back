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
public class PopBoardScheduler {

    private final PopHistoryRepository popHistoryRepository;

    @Transactional
    @Scheduled(cron = "0 * * * * *") // 매 1분마다
    public void updateBoardPopStatus() {
        LocalDateTime now = LocalDateTime.now();

        try {
            // FEATURED_BOARD 처리 (현재 시간 - 10분 이전의 데이터)
            int boardCount = popHistoryRepository.updateBoard(now, now.minusMinutes(10));

            // 로그 대박 터지는거 방지하기 위해 실제로 처리가 됐을때만 로그 찍기
            if (boardCount > 0) {
                log.info("pop 상태 처리 완료: FEATURED_BOARD: {}건", boardCount);
            }
        } catch (Exception e) {
            log.error("PopStatus 정리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }
}
