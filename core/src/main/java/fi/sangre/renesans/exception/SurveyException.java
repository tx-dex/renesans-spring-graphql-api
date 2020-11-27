package fi.sangre.renesans.exception;

import org.springframework.lang.NonNull;

//TODO: change to checked exception
public class SurveyException extends RuntimeException {
    public SurveyException(@NonNull final String message) {
        super(message);
    }
}
