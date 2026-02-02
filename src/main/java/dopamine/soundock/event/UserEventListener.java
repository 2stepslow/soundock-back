package dopamine.soundock.event;

import dopamine.soundock.dto.request.VerificationEmailRequest;
import dopamine.soundock.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final AuthService authService;

    /**
     * 기본적으로 EventListener은 특정한 이벤트가 발생했을 때 수신하여 @EventListener 어노테이션을 적용한 메서드를 실행
     * 일반 @EventListener와 달리 @TransactionalEventListener는 '트랜잭션의 상태'에 따라 실행 여부 결정
     * phase = TransactionPhase.AFTER_COMMIT -> 회원 가입 DB 저장이 완전히 성공(COMMIT)된 직후 실행
     * 만약 가입 도중 에러가 나서 롤백되면 이메일 발송 XX
     */
    // 회원가입 스레드가 이미 커밋되서 인증 토큰 저장이 원활하게 이루어지지 않음...
    // 그래서 DB작업(인증 토큰 저장)을 안전하게 수행하기 위해 새로운 스레드로 작업하기 위한 비동기 처리
    @Async("threadPoolTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserSignedUpEvent(UserSignedUpEvent event) {
        // 회원 가입 성공이 확인된 이후, 실제 이메일 발송 메서드 호출
        VerificationEmailRequest request = new VerificationEmailRequest(event.email());
        authService.sendVerificationEmail(request, event.siteURL());
    }
}
