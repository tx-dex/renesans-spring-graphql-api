package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.StaticTextGroup;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class CatalystMetadataAssembler {
    private final DriverMetadataAssembler driverMetadataAssembler;
    private final QuestionMetadataAssembler questionMetadataAssembler;
    private final MultilingualUtils multilingualUtils;
    private final TranslationService translationService;

    @NonNull
    public List<CatalystMetadata> from(@NonNull final List<Catalyst> catalysts,
                                       @NonNull final StaticTextGroup textGroup) {


        final MultilingualText globalSubTitle = translationService.getPhrases(TranslationService.QUESTIONS_TRANSLATION_GROUP, TranslationService.QUESTIONS_SUB_TITLE_TRANSLATION_KEY);
        final MultilingualText globalLowEndLabel = translationService.getPhrases(TranslationService.QUESTIONS_TRANSLATION_GROUP, TranslationService.QUESTIONS_LOW_LABEL_TRANSLATION_KEY);
        final MultilingualText globalHighEndLabel = translationService.getPhrases(TranslationService.QUESTIONS_TRANSLATION_GROUP, TranslationService.QUESTIONS_HIGH_LABEL_TRANSLATION_KEY);

        final MultilingualText subTitle = Optional.ofNullable(textGroup.getTexts())
                .map(v -> v.get(TranslationService.QUESTIONS_SUB_TITLE_TRANSLATION_KEY))
                .orElse(multilingualUtils.empty());
        final MultilingualText lowEndLabel = Optional.ofNullable(textGroup.getTexts())
                .map(v -> v.get(TranslationService.QUESTIONS_LOW_LABEL_TRANSLATION_KEY))
                .orElse(multilingualUtils.empty());
        final MultilingualText highEndLabel = Optional.ofNullable(textGroup.getTexts())
                .map(v -> v.get(TranslationService.QUESTIONS_HIGH_LABEL_TRANSLATION_KEY))
                .orElse(multilingualUtils.empty());

        return catalysts.stream()
                .map(v -> from(v,
                        multilingualUtils.combine(globalSubTitle, subTitle),
                        multilingualUtils.combine(globalLowEndLabel, lowEndLabel),
                        multilingualUtils.combine(globalHighEndLabel, highEndLabel)))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public CatalystMetadata from(@NonNull final Catalyst catalyst,
                                 @NonNull final MultilingualText subTitle,
                                 @NonNull final MultilingualText lowEndLabel,
                                 @NonNull final MultilingualText highEndLabel) {
        return CatalystMetadata.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles().getPhrases())
                .descriptions(catalyst.getDescriptions().getPhrases())
                .drivers(driverMetadataAssembler.from(catalyst.getDrivers()))
                .questions(questionMetadataAssembler.fromLikert(catalyst.getQuestions(),
                        subTitle,
                        lowEndLabel,
                        highEndLabel))
                .openQuestion(null)
                .openQuestions(questionMetadataAssembler.fromOpen(catalyst.getOpenQuestions()))
                .weight(catalyst.getWeight())
                .build();
    }
}
