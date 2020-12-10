package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode(of = "value")
@ToString(of = "value")
public class CatalystId implements IdValueObject<UUID> {
    private final UUID value;

    public CatalystId(final UUID value) {
        checkArgument(value != null, "Value is required");

        this.value = value;
    }

    @Override
    public String asString() {
        return value.toString();
    }
}
