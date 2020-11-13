package fi.sangre.renesans.graphql.resolver;

import fi.sangre.renesans.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static fi.sangre.renesans.application.utils.MultilingualUtils.getText;

@Component
public class MetadataLanguageHelper {
    @NonNull
    public String getRequiredText(@NonNull final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(getText(phrases, languageTag))
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find phrase"));
    }

    @Nullable
    public String getOptionalText(@NonNull final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(getText(phrases, languageTag))
                .orElse(null);
    }
}
