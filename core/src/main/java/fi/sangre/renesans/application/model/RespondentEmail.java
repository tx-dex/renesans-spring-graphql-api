package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "value")
public class RespondentEmail implements IdValueObject<String> {
    private final String value;

    public RespondentEmail(final String value) {
        this.value = Objects.requireNonNull(value, "value is required");
    }

    @Override
    public String asString() {
        return value;
    }
}
