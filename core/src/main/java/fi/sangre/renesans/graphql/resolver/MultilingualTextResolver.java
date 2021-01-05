package fi.sangre.renesans.graphql.resolver;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

import static fi.sangre.renesans.application.utils.MultilingualUtils.getText;

@Component
public class MultilingualTextResolver {
    @NonNull
    public String getRequiredText(@Nullable final MultilingualText text, @NonNull final String languageTag) {
        return Optional.ofNullable(text)
                .map(MultilingualText::getPhrases)
                .map(v -> getText(v, languageTag))
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find phrase"));
    }

    @NonNull
    public String getRequiredText(@Nullable final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(phrases)
                .map(v -> getText(v, languageTag))
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find phrase"));
    }

    @Nullable
    public String getOptionalText(@Nullable final MultilingualText text, @NonNull final String languageTag) {
        return Optional.ofNullable(text)
                .map(MultilingualText::getPhrases)
                .map(v -> getText(v, languageTag))
                .orElse(null);
    }

    @Nullable
    public String getOptionalText(@Nullable final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(phrases)
                .map(v -> getText(v, languageTag))
                .orElse(null);
    }
}
