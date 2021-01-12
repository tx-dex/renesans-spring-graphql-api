package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@Getter
@EqualsAndHashCode(of = "value")
public class ParameterId implements IdValueObject<UUID> {
    public static final ParameterId GLOBAL_YOU_PARAMETER_ID = new ParameterId(UUID.fromString("3b13b3f5-f418-408c-a451-39cd81fa9d89"));

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

    @Nullable
    public static Set<ParameterId> fromUUID(@Nullable final Set<UUID> ids) {
        if (ids == null) {
            return null;
        } else {
            return ids.stream()
                    .filter(Objects::nonNull)
                    .map(ParameterId::new)
                    .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

        }
    }
}
