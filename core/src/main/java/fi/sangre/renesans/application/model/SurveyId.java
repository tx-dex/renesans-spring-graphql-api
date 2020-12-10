package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode(of = "value")
public class SurveyId implements IdValueObject<UUID> {
    private final UUID value;

    public SurveyId(UUID value) {
        checkArgument(value != null, "value is required");

        this.value = value;
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String asString() {
        return value.toString();
    }
}
