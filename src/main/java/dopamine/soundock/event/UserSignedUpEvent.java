package dopamine.soundock.event;

/**
 * 회원가입 완료 이벤트
 * 비즈니스 로직을 담지 않고, 처리에 필요한 최소한의 데이터만 전달
 */
public record UserSignedUpEvent(String email, String siteURL) {
}
