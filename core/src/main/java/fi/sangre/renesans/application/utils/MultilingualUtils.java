package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j

@Component
public class MultilingualUtils {
    private static final String DEFAULT_LANGUAGE_TAG = "en";
    private static final MultilingualText EMPTY = new MultilingualText(ImmutableMap.of());

    public static int compare(@NonNull final Map<String, String> e1,
                              @NonNull final Map<String, String> e2,
                              @NonNull final String languageTag) {
        return StringUtils.compare(getText(e1, languageTag), getText(e2, languageTag), true);
    }

    @Nullable
    public static String getText(@NonNull final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(phrases.get(languageTag))
                .orElseGet(() -> Optional.ofNullable(phrases.get(DEFAULT_LANGUAGE_TAG))
                        .orElseGet(() -> phrases.values().stream().findAny()
                                .orElse(null)));
    }

    @NonNull
    public MultilingualText create(@Nullable final Map<String, String> phrases) {
        return combineMaps(null, phrases);
    }

    @NonNull
    public MultilingualText create(@Nullable final String phrase, @NonNull final String languageTag) {
        if (phrase != null) {
            final Map<String, String> phrases = new LinkedHashMap<>();
            phrases.put(languageTag, StringUtils.trimToNull(phrase));
            return new MultilingualText(Collections.unmodifiableMap(phrases));
        } else {
            return EMPTY;
        }

    }

    @NonNull
    public MultilingualText empty() {
        return EMPTY;
    }

    @NonNull
    public MultilingualText combine(@Nullable final MultilingualText existing, @Nullable final MultilingualText input) {
        return combineMaps(existing != null ? existing.getPhrases() : null, input != null ? input.getPhrases() : null);
    }

    @NonNull
    private MultilingualText combineMaps(@Nullable final Map<String, String> existing, @Nullable final Map<String, String> input) {
        final Map<String, String> phrases = new LinkedHashMap<>();
        if (existing != null) {
            phrases.putAll(existing);
        }

        if (input != null) {
            input.forEach((languageTag, phrase) -> {
                if (phrase == null) {
                    phrases.remove(languageTag);
                } else {
                    phrases.put(languageTag, phrase);
                }
            });
        }

        return new MultilingualText(Collections.unmodifiableMap(phrases));
    }
}
