package dopamine.soundock.exceptions;

import org.springframework.http.HttpStatus;

public class AuthRejectedException extends CustomException {
    public AuthRejectedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED);
    }
}
