package org.example.reactor;

public class RidesHandlingException extends RuntimeException {

    public RidesHandlingException(String message) {
        super(message);
    }

    public RidesHandlingException(String message, Throwable cause) {
        super(message, cause);
    }
}
