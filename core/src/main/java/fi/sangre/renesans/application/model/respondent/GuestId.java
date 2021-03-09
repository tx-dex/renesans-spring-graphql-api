package fi.sangre.renesans.application.model.respondent;

import fi.sangre.renesans.application.model.IdValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@Getter
@EqualsAndHashCode(of = "value")
public class GuestId implements IdValueObject<UUID> {
    private final UUID value;

    public GuestId(UUID value) {
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

    @NonNull
    public static Set<UUID> toUUIDs(@NonNull final Set<GuestId> ids) {
        return ids.stream()
                .map(GuestId::getValue)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }
}
