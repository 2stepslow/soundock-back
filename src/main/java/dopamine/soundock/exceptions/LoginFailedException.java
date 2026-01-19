package dopamine.soundock.exceptions;

import org.springframework.http.HttpStatus;

public class LoginFailedException extends CustomException {
    public LoginFailedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
