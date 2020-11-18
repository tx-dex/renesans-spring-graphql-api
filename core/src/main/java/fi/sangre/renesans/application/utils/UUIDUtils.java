package fi.sangre.renesans.application.utils;

import org.springframework.lang.Nullable;

import java.util.Optional;
import java.util.UUID;

public class UUIDUtils {
    @Nullable
    public static UUID from(@Nullable final String value) {
        return Optional.ofNullable(value)
                .map(UUID::fromString)
                .orElse(null);
    }
}
