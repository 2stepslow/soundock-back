package dopamine.soundock.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends CustomException {
    public DuplicateEmailException() {
        super("이미 사용 중인 이메일 입니다.", HttpStatus.CONFLICT);
    }
}
