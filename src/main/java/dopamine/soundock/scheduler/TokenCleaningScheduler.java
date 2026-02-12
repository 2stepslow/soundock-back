package dopamine.soundock.scheduler;

import dopamine.soundock.repository.AccessTokenBlacklistRepository;
import dopamine.soundock.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleaningScheduler {
    /*
    매일 새벽 01시에 'RefreshToken' 과 'AccessTokenBlacklist` 테이블 내
    만료기간이 지난 데이터를 물리 삭제하는 스케쥴러
     */
    private final RefreshTokenRepository refreshTokenRepository;
    private final AccessTokenBlacklistRepository accessTokenBlacklistRepository;

    @Transactional
    @Scheduled(cron = "0 0 1 * * *") // 매일 새벽 1시 정각
    public void cleanTokens() {
        LocalDateTime now = LocalDateTime.now();
        log.info("토큰 정리 스케줄러가 실행됩니다. 실행 시간 : {}", now);
        try {
            // 1. RefreshToken 테이블에서, 지금 시간보다 이전인 데이터들을 찾아서 곧바로 지운다.
            refreshTokenRepository.deleteAllByExpirationAtBefore(now);

            // 2. AccessTokenBlacklist 테이블에서, 만료 시간이 지난 데이터들을 찾아서 모두 지운다.
            accessTokenBlacklistRepository.deleteAllByExpirationAtBefore(now);
            log.info("토큰 정리 스케줄러 작업이 성공적으로 마무리 되었습니다. 실행 시간 : {}", now);
        } catch (Exception e) {
            log.error("토큰 정리 중 오류가 발생했습니다 : {}", e.getMessage(), e);
        }
    }
}
