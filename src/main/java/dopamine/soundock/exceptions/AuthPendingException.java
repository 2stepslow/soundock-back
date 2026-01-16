package dopamine.soundock.exceptions;

import org.springframework.http.HttpStatus;

public class AuthPendingException extends CustomException {
    public AuthPendingException(String message) {
        super(message, HttpStatus.OK);
    }
}
