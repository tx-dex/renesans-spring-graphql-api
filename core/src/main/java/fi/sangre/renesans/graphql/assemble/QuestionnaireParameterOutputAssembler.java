package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.parameter.*;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.output.parameter.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireParameterOutputAssembler {
    @NonNull
    public List<QuestionnaireParameterOutput> from(@Nullable final List<Parameter> parameters) {
        return Optional.ofNullable(parameters)
                .orElse(ImmutableList.of()).stream()
                .map(e -> from(e, ImmutableMap.of()))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<QuestionnaireParameterOutput> from(@Nullable final List<Parameter> parameters, @NonNull final Map<ParameterId, ParameterItemAnswer> answers) {
        return Optional.ofNullable(parameters)
                .orElse(ImmutableList.of()).stream()
                .map(e -> from(e, answers))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireParameterOutput from(@NonNull final Parameter parameter, @NonNull final Map<ParameterId, ParameterItemAnswer> answers) {
        final ParameterId answerId = Optional.ofNullable(answers.get(parameter.getId()))
                .map(ParameterItemAnswer::getResponse)
                .orElse(null);

        final QuestionnaireParameterOutput output;
        if (parameter instanceof ListParameter) {
            output = from((ListParameter) parameter, answerId);
        } else if (parameter instanceof TreeParameter) {
            output = from((TreeParameter) parameter, answerId);
        } else {
            throw new SurveyException("Invalid parameter type");
        }

        if (answerId != null) {
            output.setSelectedAnswer(ImmutableSet.of(answerId.getValue()));
        } else {
            output.setSelectedAnswer(ImmutableSet.of());
        }

        return output;
    }

    @NonNull
    private QuestionnaireListParameterOutput from(@NonNull final ListParameter parameter, @Nullable final ParameterId answerId) {
        return QuestionnaireListParameterOutput.builder()
                .value(parameter.getId().toString())
                .labels(parameter.getLabel().getPhrases())
                .children(parameter.getValues().stream()
                        .map(v -> QuestionnaireParameterItemOutput.builder()
                                .value(v.getId().toString())
                                .labels(v.getLabel().getPhrases())
                                .checked(v.getId().equals(answerId))
                        .build())
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .answered(answerId != null)
                .build();
    }

    @NonNull
    private QuestionnaireTreeParameterOutput from(@NonNull final TreeParameter parameter, @Nullable final ParameterId answerId) {
        return QuestionnaireTreeParameterOutput.builder()
                .value(parameter.getId().toString())
                .labels(parameter.getLabel().getPhrases())
                .children(parameter.getChildren().stream()
                        .map(e -> from(e, answerId))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .answered(answerId != null)
                .build();
    }

    @NonNull
    private QuestionnaireParameterChildOutput from(@NonNull final ParameterChild child, @Nullable final ParameterId answerId) {
        if (child instanceof ParameterItem) {
            return QuestionnaireParameterItemOutput.builder()
                    .value(child.getId().toString())
                    .labels(child.getLabel().getPhrases())
                    .checked(child.getId().equals(answerId))
                    .build();
        } else if (child instanceof TreeParameter) {
            return from((TreeParameter) child, answerId);
        } else {
            throw new SurveyException("Invalid type");
        }
    }
}
