package fi.sangre.renesans.persistence.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.DriverWeight;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.persistence.model.metadata.questions.LikertQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionMetadataAssembler {
    private static final Double DEFAULT_QUESTION_DRIVER_WEIGHT = 0d;

    @NonNull
    public List<QuestionMetadata> from(@NonNull final List<LikertQuestion> questions) {
        return questions.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionMetadata from(@NonNull final LikertQuestion question) {
        return LikertQuestionMetadata.builder()
                .id(question.getId().getValue())
                .titles(question.getTitles().getPhrases())
                .driverWeights(fromWeights(question.getWeights()))
                .build();
    }

    @NonNull
    private Map<Long, Double> fromWeights(@Nullable final List<DriverWeight> weights) {
        return Optional.ofNullable(weights)
                .orElse(ImmutableList.of())
                .stream()
                .filter(e -> !DEFAULT_QUESTION_DRIVER_WEIGHT.equals(e.getWeight()))
                .collect(collectingAndThen(toMap(e -> e.getDriver().getId(), DriverWeight::getWeight), Collections::unmodifiableMap));
    }
}
