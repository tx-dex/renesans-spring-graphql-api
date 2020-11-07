package fi.sangre.renesans.graphql.error;

public enum ErrorCodes {
    UNKNOWN_ERROR(255);

    private final Integer value;

    ErrorCodes(final Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
