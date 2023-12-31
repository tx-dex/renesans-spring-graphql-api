package fi.sangre.renesans.service;

import com.google.common.base.Splitter;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.TranslationMap;
import fi.sangre.renesans.application.model.TranslationText;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    public static final String DEFAULT_LANGUAGE = "en";
    public static final String QUESTIONS_TRANSLATION_GROUP = "questions";
    public static final String QUESTIONS_LOW_LABEL_TRANSLATION_KEY = "unacceptable";
    public static final String QUESTIONS_HIGH_LABEL_TRANSLATION_KEY = "excellent";

    private static final String SPACE = " ";
    private static final Splitter TITLE_SPLITTER = Splitter.on("_")
            .omitEmptyStrings()
            .trimResults();
    private final Map<String, TranslationMap> translations;
    private final MultilingualUtils multilingualUtils;

    public String getLanguageTagOrDefault(@Nullable final String languageTag) {
        final String languageTagOrDefault = Optional.ofNullable(languageTag)
                .map(StringUtils::trimToNull)
                .map(StringUtils::lowerCase)
                .orElse(DEFAULT_LANGUAGE);

        if (translations.containsKey(languageTagOrDefault)) {
            return languageTagOrDefault;
        } else {
            log.warn("Invalid language tag: '{}'", languageTag);
            return DEFAULT_LANGUAGE;
        }
    }

    @NonNull
    public Map<String, Map<String, TranslationText>> getTranslations(@NonNull final String languageTag) {
        return Optional.ofNullable(translations.get(languageTag))
                .orElseGet(() -> translations.get(DEFAULT_LANGUAGE))
                .getTranslations();
    }

    @NonNull
    public MultilingualText getPhrases(@NonNull final String group, @NonNull final String phrase) {

        return multilingualUtils.create(translations.entrySet().stream()
                .map(v -> getTranslations(v.getKey(), v.getValue(), group, phrase))
                .filter(v -> Objects.nonNull(v.getRight()))
                .collect(toMap(Pair::getLeft, Pair::getRight)));
    }

    @NonNull
    private Pair<String, String> getTranslations(@NonNull final String languageTag,
                                                 @NonNull final TranslationMap map,
                                                 @NonNull final String group,
                                                 @NonNull final String phrase) {

        return Pair.of(languageTag, Optional.ofNullable(map.getTranslations())
                .map(v -> v.get(group))
                .map(v -> v.get(phrase))
                .map(TranslationText::getText)
                .orElse(null));
    }

    @NonNull
    public String getTitle(@NonNull final String id, @Nullable final String title) {
        return Optional.ofNullable(title)
                .orElseGet(() -> {
                    final StringBuilder titleBuilder = new StringBuilder();
                    TITLE_SPLITTER.split(id).forEach(part ->
                            titleBuilder.append(StringUtils.capitalize(part)).append(SPACE));

                    return titleBuilder.toString();
                });
    }
}
