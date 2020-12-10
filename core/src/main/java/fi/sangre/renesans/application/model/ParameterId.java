package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;

@Getter
@EqualsAndHashCode(of = "value")
public class ParameterId implements IdValueObject<UUID> {
    private final UUID value;

    public ParameterId(final UUID value) {
        checkArgument(value != null, "value is required");

        this.value = value;
    }

    public ParameterId(final ParameterId id) {
        checkArgument(id != null, "id is required");

        this.value = id.getValue();
    }

    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public String asString() {
        return value.toString();
    }

    @Nullable
    public static ParameterId from(@Nullable final String value) {
        return Optional.ofNullable(value)
                .map(UUID::fromString)
                .map(ParameterId::new)
                .orElse(null);
    }
}
