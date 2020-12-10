package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Objects;

@Getter
@EqualsAndHashCode(of = "value")
@ToString(of = "value")
public class DriverId implements IdValueObject<Long> {
    private final Long value;

    public DriverId(final Long value) {
        this.value = Objects.requireNonNull(value, "Driver id value must not be null");
    }

    @Override
    public String asString() {
        return value.toString();
    }
}
