package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.question.LikertQuestionInput;
import fi.sangre.renesans.graphql.input.question.OpenQuestionInput;
import fi.sangre.renesans.persistence.model.metadata.questions.LikertQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.OpenQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
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
public class QuestionAssembler {
    private static final double DEFAULT_CATALYST_DRIVER_WEIGHT = 0d;
    private final MultilingualUtils multilingualUtils;

    @Nullable
    public List<OpenQuestion> fromOpenInput(@NonNull final CatalystId catalystId, @Nullable final List<OpenQuestionInput> inputs, @NonNull final String languageTag) {
        if (inputs != null) {
            if (new HashSet<>(inputs).size() != inputs.size()) {
                throw new SurveyException("Duplicated questions keys in the input");
            }

            return inputs.stream()
                    .map(e -> from(catalystId, e, languageTag))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return null;
        }
    }

    @NonNull
    public OpenQuestion from(@NonNull final CatalystId catalystId, @NonNull final OpenQuestionInput input, @NonNull final String languageTag) {
        final QuestionId questionId = Optional.ofNullable(input.getId())
                .map(QuestionId::new)
                .orElse(null);

        return OpenQuestion.builder()
                .id(questionId)
                .catalystId(catalystId)
                .titles(multilingualUtils.create(input.getTitle(), languageTag))
                .build();
    }

    @Nullable
    public List<LikertQuestion> fromLikertInput(@NonNull final CatalystId catalystId, @Nullable final List<LikertQuestionInput> inputs, @NonNull final String languageTag) {
        if (inputs != null) {
            if (new HashSet<>(inputs).size() != inputs.size()) {
                throw new SurveyException("Duplicated questions keys in the input");
            }

            return inputs.stream()
                    .map(e -> from(catalystId, e, languageTag))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return null;
        }
    }

    @NonNull
    public LikertQuestion from(@NonNull final CatalystId catalystId, @NonNull final LikertQuestionInput input, @NonNull final String languageTag) {
        final QuestionId questionId = Optional.ofNullable(input.getId())
                .map(QuestionId::new)
                .orElse(null);

        return LikertQuestion.builder()
                .id(questionId)
                .catalystId(catalystId)
                .titles(multilingualUtils.create(input.getTitle(), languageTag))
                .subTitles(multilingualUtils.create(input.getSubTitle(), languageTag))
                .lowEndLabels(multilingualUtils.create(input.getLowEndLabel(), languageTag))
                .highEndLabels(multilingualUtils.create(input.getHighEndLabel(), languageTag))
                .build();
    }

    @NonNull
    public List<OpenQuestion> fromOpenMetadata(@NonNull final Catalyst catalyst, @Nullable final List<QuestionMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(v -> fromOpen(catalyst, v))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }


    @NonNull
    private OpenQuestion fromOpen(@NonNull final Catalyst catalyst, @NonNull final QuestionMetadata metadata) {
        if (metadata instanceof OpenQuestionMetadata) {
            return from(catalyst, (OpenQuestionMetadata) metadata);
        } else {
            // TODO: implement later if needed
            throw new SurveyException("Invalid question type");
        }
    }

    @NonNull
    public List<LikertQuestion> fromLikertMetadata(@NonNull final Catalyst catalyst,
                                                   @NonNull final Map<DriverId, Double> allDriverWeights,
                                                   @Nullable final List<QuestionMetadata> metadata,
                                                   @NonNull final MultilingualText lowEndLabel,
                                                   @NonNull final MultilingualText highEndLabel) {
        final Map<DriverId, Double> defaultDriverWeights = new LinkedHashMap<>(allDriverWeights);
        defaultDriverWeights.putAll(Optional.ofNullable(catalyst.getDrivers())
                .orElse(ImmutableList.of()).stream()
                .collect(toMap(v -> new DriverId(v.getId()), v -> DEFAULT_CATALYST_DRIVER_WEIGHT)));

        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(v -> fromLikert(catalyst,
                        Collections.unmodifiableMap(defaultDriverWeights),
                        v,
                        lowEndLabel,
                        highEndLabel))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }


    @NonNull
    private LikertQuestion fromLikert(@NonNull final Catalyst catalyst,
                                      @NonNull final Map<DriverId, Double> defaultDriverWeights,
                                      @NonNull final QuestionMetadata metadata,
                                      @NonNull final MultilingualText lowEndLabel,
                                      @NonNull final MultilingualText highEndLabel) {
        if (metadata instanceof LikertQuestionMetadata) {
            return from(catalyst, defaultDriverWeights, (LikertQuestionMetadata) metadata, lowEndLabel, highEndLabel);
        } else {
            // TODO: implement later if needed
            throw new SurveyException("Invalid question type");
        }
    }

    @NonNull
    private OpenQuestion from(@NonNull final Catalyst catalyst, @NonNull final OpenQuestionMetadata metadata) {
        return OpenQuestion.builder()
                .id(new QuestionId(metadata.getId()))
                .catalystId(catalyst.getId())
                .titles(multilingualUtils.create(metadata.getTitles()))
                .build();
    }

    @NonNull
    private LikertQuestion from(@NonNull final Catalyst catalyst,
                                @NonNull final Map<DriverId, Double> defaultDriverWeigths,
                                @NonNull final LikertQuestionMetadata metadata,
                                @NonNull final MultilingualText defaultLowEndLabel,
                                @NonNull final MultilingualText defaultHighEndLabel) {

        final MultilingualText lowEndLabel = multilingualUtils.combine(defaultLowEndLabel,
                multilingualUtils.create(metadata.getLowEndLabels()));
        final MultilingualText highEndLabel = multilingualUtils.combine(defaultHighEndLabel,
                multilingualUtils.create(metadata.getHighEndLabels()));

        final Map<DriverId, Double> driverWeights = new LinkedHashMap<>(defaultDriverWeigths);
        driverWeights.putAll(Optional.ofNullable(metadata.getDriverWeights())
                .orElse(ImmutableMap.of()).entrySet().stream()
                .collect(toMap(
                        e -> new DriverId(Long.parseLong(e.getKey())),
                        Map.Entry::getValue
                )));

        return LikertQuestion.builder()
                .id(new QuestionId(metadata.getId()))
                .catalystId(catalyst.getId())
                .titles(multilingualUtils.create(metadata.getTitles()))
                .subTitles(multilingualUtils.create(metadata.getSubTitles()))
                .lowEndLabels(lowEndLabel)
                .highEndLabels(highEndLabel)
                .weights(Collections.unmodifiableMap(driverWeights))
                .build();
    }
}