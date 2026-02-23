package dopamine.soundock.scheduler;

import dopamine.soundock.repository.BoardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class SpotlightExpireScheduler {

    private final BoardRepository boardRepository;

    // 매 10분 마다 실행 - remainingPop이 0인 Spotlight 게시글 만료 시점 기록
    @Transactional
    @Scheduled(cron = "0 */10 * * * *")
    public void markExpiredSpotlightBoards() {
        LocalDateTime now = LocalDateTime.now();

        try {
            int expiredCount = boardRepository.updateExpiredSpotlightBoards(now);

            if (expiredCount > 0) {
                log.info("Spotlight 만료 처리 완료: {}건, 만료 처리 시간: {}", expiredCount, now);
            }
        } catch (Exception e) {
            log.error("Spotlight 만료 처리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }
}
