package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.exception.SurveyException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j

@Component
public class MultilingualUtils {
    private static final Set<String> VALID_LANGUAGE_TAGS = ImmutableSet.of("en", "fi", "lv", "no", "sv");
    private static final String DEFAULT_LANGUAGE_TAG = "en";
    private static final MultilingualText EMPTY = new MultilingualText(ImmutableMap.of());

    public static int compare(@NonNull final Map<String, String> e1,
                              @NonNull final Map<String, String> e2,
                              @NonNull final String languageTag) {
        return StringUtils.compare(getText(e1, languageTag), getText(e2, languageTag), true);
    }

    @NonNull
    public String getTextOrDefault(@NonNull final MultilingualText text, @NonNull final String languageTag, @NonNull final String defaultText) {
        final Map<String, String> phrases = Objects.requireNonNull(text.getPhrases());

        return Optional.ofNullable(phrases.get(languageTag))
                .orElseGet(() -> Optional.ofNullable(phrases.get(DEFAULT_LANGUAGE_TAG))
                        .orElseGet(() -> phrases.values().stream().findAny()
                                .orElse(defaultText)));
    }

    @Nullable
    public static String getText(@NonNull final Map<String, String> phrases, @NonNull final String languageTag) {
        return Optional.ofNullable(phrases.get(languageTag))
                .orElseGet(() -> Optional.ofNullable(phrases.get(DEFAULT_LANGUAGE_TAG))
                        .orElseGet(() -> phrases.values().stream().findAny()
                                .orElse(null)));
    }

    public void checkLanguageTag(@NonNull final String languageTag) {
        if (StringUtils.isBlank(languageTag)) {
            log.warn("Language code must not be empty");
            throw new SurveyException("Language code must not be empty");
        } else if (!VALID_LANGUAGE_TAGS.contains(languageTag)) {
            log.warn("Invalid language tag. Must be 'fi' or 'en' but is '{}'", languageTag);
            throw new SurveyException("Invalid language tag. Must be 'fi' or 'en' but is '" + languageTag + "'");
        }
    }

    public void checkDuplicates(@NonNull final List<MultilingualText> texts, @NonNull final String message) {
        for(final String languageTag : VALID_LANGUAGE_TAGS) {
            final List<String> all = new ArrayList<>();
            final Set<String> unique = new HashSet<>();
            for(final MultilingualText text : texts) {
                final String phrase = StringUtils.lowerCase(text.getPhrase(languageTag));
                if (phrase != null) {
                    all.add(phrase);
                    unique.add(phrase);
                }
            }

            if (all.size() != unique.size()) {
                throw new SurveyException(message);
            }
        }
    }

    @Nullable
    public Map<String, String> emptyToNull(@Nullable final MultilingualText text) {
        if (text == null || text.isEmpty()) {
            return null;
        } else {
            return text.getPhrases();
        }
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
    public MultilingualText combine(@Nullable final MultilingualText existing, @Nullable final Map<String, String> input) {
        return combineMaps(existing != null ? existing.getPhrases() : null, input);
    }

    public boolean isSameText(@Nullable final MultilingualText v1, @Nullable final MultilingualText v2) {
        final Map<String, String> phrases1 = Optional.ofNullable(v1)
                .map(v -> v.getPhrases())
                .orElse(EMPTY.getPhrases());
        final Map<String, String> phrases2 = Optional.ofNullable(v2)
                .map(v -> v.getPhrases())
                .orElse(EMPTY.getPhrases());

        return phrases1.equals(phrases2);
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
