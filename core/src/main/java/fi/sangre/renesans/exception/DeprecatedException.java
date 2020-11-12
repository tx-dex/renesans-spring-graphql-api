package fi.sangre.renesans.exception;

public class DeprecatedException extends RuntimeException {
    public DeprecatedException() {
        super("Usage of deprecated method");
    }
}
