package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.exception.SurveyException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

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
}
