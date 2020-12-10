package fi.sangre.renesans.application.model.respondent;

import fi.sangre.renesans.application.model.IdValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode(of = "value")
public class RespondentId implements IdValueObject<UUID> {
    private final UUID value;

    public RespondentId(UUID value) {
        checkArgument(value != null, "Id is required");

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
