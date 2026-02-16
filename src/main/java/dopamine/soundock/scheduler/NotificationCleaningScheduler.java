package dopamine.soundock.scheduler;

import dopamine.soundock.entity.Notifications;
import dopamine.soundock.repository.NotificationRepository;
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
public class NotificationCleaningScheduler {

    private final NotificationRepository notificationRepository;

    // 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanOldNotifications() {
        LocalDateTime now = LocalDateTime.now();
        log.info("알림 정리 스케줄러가 실행됩니다. 실행 시간 : {}", now);

        try {
            // 1개월 이상 된 알림 삭제
            LocalDateTime thresholdDate = now.minusMonths(1);
            List<Notifications> oldNotifications = notificationRepository.findByCreatedDatetimeBefore(thresholdDate);
            int deletedCount = oldNotifications.size();

            notificationRepository.deleteAll(oldNotifications);

            log.info("알림 정리 스케줄러 작업이 성공적으로 마무리 되었습니다. 삭제된 알림 개수 : {}, 실행 시간 : {}", deletedCount, now);
        } catch (Exception e) {
            log.error("알림 정리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }
}
