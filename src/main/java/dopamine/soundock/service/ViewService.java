package dopamine.soundock.service;

import dopamine.soundock.global.constants.AppConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class ViewService {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 조회수 증가 중복 검사 (Redis 저장 이용)
     * true 면 +1 증가, false 면 0
     */
    public boolean checkView(int boardId, String email, String clientIp) {
        if (email == null && (clientIp == null || clientIp.isEmpty())) {
            throw new IllegalArgumentException("이메일 또는 Ip 값이 필요합니다.");
        }

        // userId가 있다면 user:{userId}
        // userId가 없다면 ip:{clientIp}
        String identify = (email != null) ? "user:" + email : "ip:" + clientIp;
        // 합쳐서 키로 만듦
        String key = AppConstants.Redis.KEY_PREFIX + boardId + ":" + identify;

        // setIfAbsent : 키가 없을 때만 저장, opsForValue() : String(Key-Value) 조작 기능
        // Duration.ofHours : 해당 시간이 지나면 Redis에서 삭제
        // 최종 저장 형태 : board:view:user:{userId} - visited <- 24시간 동안 남아있고 있는 동안은 isFirstVisit = false
        Boolean isFirstVisit = redisTemplate.opsForValue()
                .setIfAbsent(key, "visited", Duration.ofHours(AppConstants.Time.VIEW_COOLDOWN_HOURS));

        if (isFirstVisit != null && isFirstVisit) {
            return true;
        }

        return false;
    }
}
