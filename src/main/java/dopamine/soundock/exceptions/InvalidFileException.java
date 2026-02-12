package dopamine.soundock.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidFileException extends CustomException {
    public InvalidFileException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
}