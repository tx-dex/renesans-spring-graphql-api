package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j

@Component
public class UUIDUtils {
    @Nullable
    public UUID from(@Nullable final String value) {
        try {
            return Optional.ofNullable(value)
                    .map(UUID::fromString)
                    .orElse(null);
        } catch (final Exception ex) {
            log.warn("Cannot convert '{}' to UUID", value, ex);
            throw new SurveyException("Not a UUID value");
        }
    }

    public UUID generate(@Nullable final Set<UUID> reserved) {
        if (reserved == null) {
            return UUID.randomUUID();
        } else {
            UUID generated = UUID.randomUUID();

            while (reserved.contains(generated)) {
                generated = UUID.randomUUID();
            }

            return generated;
        }
    }

    @Nullable
    public <T extends IdValueObject<UUID>> Set<UUID> toUUIDs(@Nullable final Set<T> ids) {
        if (ids == null) {
            return null;
        } else {
            return ids.stream()
                    .map(IdValueObject::getValue)
                    .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
        }
    }
}
