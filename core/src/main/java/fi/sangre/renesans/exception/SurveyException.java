package fi.sangre.renesans.exception;

import org.springframework.lang.NonNull;

public class SurveyException extends RuntimeException {
    public SurveyException(@NonNull final String message) {
        super(message);
    }
}
