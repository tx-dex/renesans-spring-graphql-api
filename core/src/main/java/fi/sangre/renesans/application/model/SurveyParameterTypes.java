package fi.sangre.renesans.application.model;

import org.springframework.lang.NonNull;

public enum SurveyParameterTypes {
    TREE("tree"),
    LIST("list");

    private final String value;

    SurveyParameterTypes(@NonNull final String value) {
        this.value = value;
    }

    @NonNull
    public String getValue() {
        return value;
    }
}
