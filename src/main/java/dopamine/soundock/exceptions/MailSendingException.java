package dopamine.soundock.exceptions;

import org.springframework.http.HttpStatus;

// 이메일 발송 실패 예외
public class MailSendingException extends CustomException {
    public MailSendingException() {
        super("인증 이메일 발송 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
