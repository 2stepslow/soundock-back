package dopamine.soundock.scheduler;

import dopamine.soundock.entity.Messages;
import dopamine.soundock.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageCleaningScheduler {
    private final MessageRepository messageRepository;

    // 매일 자정에 실행
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanOldMessages() {

        LocalDateTime now = LocalDateTime.now();
        log.info("메세지 정리 스케줄러가 실행됩니다. 실행 시간 : {}", now);

        try {
            // 3개월 이상 된 메시지 삭제
            LocalDateTime thresholdDate = now.minusMonths(3);
            List<Messages> oldMessages = messageRepository.findByCreatedDatetimeBefore(thresholdDate);

            int deletedCount = oldMessages.size();
            messageRepository.deleteAll(oldMessages);

            log.info("메세지 정리 스케줄러 작업이 성공적으로 마무리 되었습니다. 삭제된 메세지 개수 : {}, 실행 시간 : {}", deletedCount, now);
        } catch (Exception e) {
            log.error("메세지 정리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }

}
