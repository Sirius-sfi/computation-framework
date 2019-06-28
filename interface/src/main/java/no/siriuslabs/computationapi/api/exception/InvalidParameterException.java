package no.siriuslabs.computationapi.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception class to be used if a parameter is not valid.
 */
@ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
public class InvalidParameterException extends RuntimeException {

    /**
     * Creates an instance using the given message.
     */
    public InvalidParameterException(String message) {
        super(message);
    }
}
