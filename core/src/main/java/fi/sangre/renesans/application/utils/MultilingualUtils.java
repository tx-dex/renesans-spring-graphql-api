package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import fi.sangre.renesans.persistence.model.metadata.MultilingualMetadata;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

public class MultilingualUtils {
    private static final String DEFAULT_LANGUAGE_TAG = "en";

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
    public static Map<String, String> create(@Nullable final String phrase, @NonNull final String languageTag) {
        return combine((Map<String, String>) null, phrase, languageTag);
    }

    @NonNull
    public static Map<String, String> combine(@Nullable final Map<String, String> phrases, @Nullable final String phrase, @NonNull final String languageTag) {
        final Map<String, String> combined;
        if (phrases == null) {
            combined = Maps.newHashMap();
        } else {
            combined = Maps.newHashMap(phrases);
        }

        if (phrase != null) {
            combined.put(languageTag, phrase);
        }

        return ImmutableMap.copyOf(combined);
    }

    @NonNull
    public static MultilingualMetadata combine(@Nullable final MultilingualMetadata metadata, @Nullable final String phrase, @NonNull final String languageTag) {
        if (metadata == null) {
            return new MultilingualMetadata(create(phrase, languageTag));
        } else {
            return new MultilingualMetadata(combine(metadata.getPhrases(), phrase, languageTag));
        }
    }
}
