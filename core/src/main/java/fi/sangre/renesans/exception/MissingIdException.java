package fi.sangre.renesans.exception;

import org.springframework.lang.NonNull;

import java.util.function.Supplier;

public class MissingIdException extends RuntimeException {
    public static final Supplier<String> MESSAGE_SUPPLIER = () -> "Missing id on object";

    public MissingIdException(@NonNull final String message) {
        super(message);
    }
}
