package fi.sangre.renesans.graphql.resolver;

import fi.sangre.renesans.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
public class MetadataLanguageHelper {
    private static final String DEFAULT_LANGUAGE_TAG = "en";

    @NonNull
    public String getRequiredText(@NonNull final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(phrases.get(languageTag))
                .orElseGet(() -> Optional.ofNullable(phrases.get(DEFAULT_LANGUAGE_TAG))
                        .orElseGet(() -> phrases.values().stream().findAny()
                                .orElseThrow(() -> new ResourceNotFoundException("Cannot find phrase"))));


    }

    @Nullable
    public String getOptionalText(@NonNull final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(phrases.get(languageTag))
                .orElseGet(() -> Optional.ofNullable(phrases.get(DEFAULT_LANGUAGE_TAG))
                        .orElseGet(() -> phrases.values().stream().findAny()
                                .orElse(null)));


    }
}
