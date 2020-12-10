package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.question.LikertQuestionInput;
import fi.sangre.renesans.persistence.model.metadata.questions.LikertQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionAssembler {
    private final MultilingualUtils multilingualUtils;

    @Nullable
    public List<LikertQuestion> fromInput(@NonNull final CatalystId catalystId, @Nullable final List<LikertQuestionInput> inputs, @NonNull final String languageTag) {
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
                .build();
    }

    @NonNull
    public List<LikertQuestion> fromMetadata(@Nullable final List<QuestionMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private LikertQuestion from(@NonNull final QuestionMetadata metadata) {
        if (metadata instanceof LikertQuestionMetadata) {
            return from((LikertQuestionMetadata) metadata);
        } else {
            // TODO: implement later
            throw new SurveyException("Invalid question type");
        }
    }

    @NonNull
    private LikertQuestion from(@NonNull final LikertQuestionMetadata metadata) {
        return LikertQuestion.builder()
                .id(new QuestionId(metadata.getId()))
                .titles(multilingualUtils.create(metadata.getTitles()))
                .build();
    }
}