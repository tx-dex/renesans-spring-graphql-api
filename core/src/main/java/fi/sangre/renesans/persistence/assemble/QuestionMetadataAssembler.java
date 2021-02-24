package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
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
public class QuestionMetadataAssembler {
    private static final Double DEFAULT_QUESTION_DRIVER_WEIGHT = 0d;

    @NonNull
    public List<QuestionMetadata> fromOpen(@NonNull final List<OpenQuestion> questions) {
        return questions.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionMetadata from(@NonNull final OpenQuestion question) {
        return OpenQuestionMetadata.builder()
                .id(question.getId().getValue())
                .titles(question.getTitles().getPhrases())
                .build();
    }

    @NonNull
    public List<QuestionMetadata> fromLikert(@NonNull final List<LikertQuestion> questions) {
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

    @Nullable
    private Map<String, Double> fromWeights(@Nullable final Map<DriverId, Double> weights) {
        return Optional.ofNullable(weights)
                .map(map -> map.entrySet().stream()
                        .filter(e ->  !DEFAULT_QUESTION_DRIVER_WEIGHT.equals(e.getValue()))
                        .collect(collectingAndThen(toMap(
                                e -> e.getKey().asString(),
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                LinkedHashMap::new
                        ), Collections::unmodifiableMap)))
                .orElse(null);
    }
}
