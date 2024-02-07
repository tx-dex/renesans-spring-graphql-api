package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.MissingIdException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.CatalystInput;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class CatalystAssembler {
    private static final double DEFAULT_ALL_DRIVER_WEIGHT = 0d;
    private final QuestionAssembler questionAssembler;
    private final DriverAssembler driverAssembler;
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<Catalyst> fromInputs(@NonNull final List<CatalystInput> inputs, @NonNull final String languageTag) {
        if (new HashSet<>(inputs).size() != inputs.size()) {
            throw new SurveyException("Duplicated catalysts' keys in the input");
        }

        return inputs.stream()
                .map(e -> from(e, languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Catalyst from(@NonNull CatalystInput input, @NonNull final String languageTag) {
        final CatalystId catalystId = new CatalystId(Objects.requireNonNull(input.getId(), "Catalyst id not provided in the input"));

        return Catalyst.builder()
                .id(catalystId)
                .titles(multilingualUtils.create(input.getTitle(), languageTag))
                .descriptions(multilingualUtils.create(input.getDescription(), languageTag))
                .drivers(driverAssembler.fromInput(input.getDrivers(), languageTag))
                .questions(Optional.ofNullable(input.getQuestions())
                        .map(questions -> questionAssembler.fromLikertInput(catalystId, questions, languageTag))
                        .orElse(null))
                .openQuestions(Optional.ofNullable(input.getOpenQuestions())
                        .map(questions -> questionAssembler.fromOpenInput(catalystId, questions, languageTag))
                        .orElse(null))
                .build();
    }

    @NonNull
    public List<Catalyst> fromMetadata(@Nullable final List<CatalystMetadata> metadata,
                                       @NonNull final StaticTextGroup textGroup) {
        final MultilingualText lowEndLabel = Optional.ofNullable(textGroup.getTexts())
                .map(v -> v.get(TranslationService.QUESTIONS_LOW_LABEL_TRANSLATION_KEY))
                .orElse(multilingualUtils.empty());
        final MultilingualText highEndLabel = Optional.ofNullable(textGroup.getTexts())
                .map(v -> v.get(TranslationService.QUESTIONS_HIGH_LABEL_TRANSLATION_KEY))
                .orElse(multilingualUtils.empty());

        final Map<DriverId, Double> defaultDriverWeights = Optional.ofNullable(metadata)
                .orElse(ImmutableList.of()).stream()
                .flatMap(v -> Optional.ofNullable(v.getDrivers()).orElse(ImmutableList.of()).stream())
                .collect(toMap(v -> new DriverId(v.getId()), v -> DEFAULT_ALL_DRIVER_WEIGHT, (v1, v2) -> v2, LinkedHashMap::new));

        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(v -> from(v,
                        Collections.unmodifiableMap(defaultDriverWeights),
                        lowEndLabel,
                        highEndLabel))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Catalyst from(@NonNull final CatalystMetadata metadata,
                          @NonNull final Map<DriverId, Double> defaultAllDriverWeights,
                          @NonNull final MultilingualText lowEndLabel,
                          @NonNull final MultilingualText highEndLabel) {

        CatalystId catalystId = new CatalystId(Objects.requireNonNull(metadata.getId(), MissingIdException.MESSAGE_SUPPLIER));

        final Catalyst catalyst = Catalyst.builder()
                .id(catalystId)
                .pdfName(metadata.getPdfName())
                .titles(multilingualUtils.create(metadata.getTitles()))
                .descriptions(multilingualUtils.create(metadata.getDescriptions()))
                .drivers(driverAssembler.fromMetadata(metadata.getDrivers(), catalystId))
                .weight(metadata.getWeight())
                .build();

        final List<LikertQuestion> likertQuestions = questionAssembler.fromLikertMetadata(catalyst,
                defaultAllDriverWeights,
                metadata.getQuestions(),
                lowEndLabel,
                highEndLabel);
        final List<OpenQuestion> openQuestions = questionAssembler.fromOpenMetadata(catalyst, metadata.getOpenQuestions());

        catalyst.setQuestions(likertQuestions);
        catalyst.setOpenQuestions(openQuestions);

        return catalyst;
    }
}
