package fi.sangre.renesans.service;

import com.google.common.base.Splitter;
import fi.sangre.renesans.application.model.TranslationMap;
import fi.sangre.renesans.application.model.TranslationText;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class TranslationService {
    private static final String DEFAULT_LANGUAGE = "en";
    private static final String SPACE = " ";
    private static final Splitter TITLE_SPLITTER = Splitter.on("_")
            .omitEmptyStrings()
            .trimResults();
    private final Map<String, TranslationMap> translations;

    @NonNull
    public Map<String, Map<String, TranslationText>> getTranslations(@NonNull final String languageTag) {
        return Optional.ofNullable(translations.get(languageTag))
                .orElseGet(() -> translations.get(DEFAULT_LANGUAGE))
                .getTranslations();
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
