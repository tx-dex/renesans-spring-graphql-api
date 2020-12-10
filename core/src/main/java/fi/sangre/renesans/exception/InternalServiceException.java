package fi.sangre.renesans.exception;

import org.springframework.lang.NonNull;

public class InternalServiceException extends RuntimeException {
    private static final String PREFIX = "Internal Server Error. ";

    public InternalServiceException(@NonNull final String message) {
        super(PREFIX + message);
    }
}
