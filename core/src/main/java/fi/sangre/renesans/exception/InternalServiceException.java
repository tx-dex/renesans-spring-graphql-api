package fi.sangre.renesans.exception;

import org.springframework.lang.NonNull;

public class InternalServiceException extends RuntimeException {
    public InternalServiceException(@NonNull final String message) {
        super(message);
    }
}
