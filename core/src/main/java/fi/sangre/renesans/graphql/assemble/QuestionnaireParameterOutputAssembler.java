package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableList;
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

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireParameterOutputAssembler {
    @NonNull
    public List<QuestionnaireParameterOutput> from(@Nullable final List<Parameter> parameters) {
        if (parameters == null) {
            return ImmutableList.of();
        } else {
            return parameters.stream()
                    .map(this::from)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }
    }

    @NonNull
    private QuestionnaireParameterOutput from(@NonNull final Parameter parameter) {
        if (parameter instanceof ListParameter) {
            return from((ListParameter) parameter);
        } else if (parameter instanceof TreeParameter) {
            return from((TreeParameter) parameter);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private QuestionnaireListParameterOutput from(@NonNull final ListParameter metadata) {
        return QuestionnaireListParameterOutput.builder()
                .value(metadata.getId().toString())
                .labels(metadata.getLabel().getPhrases())
                .children(metadata.getValues().stream()
                        .map(v -> new QuestionnaireParameterItemOutput(v.getId().toString(), v.getLabel().getPhrases()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .answered(false)
                .build();
    }

    @NonNull
    private QuestionnaireTreeParameterOutput from(@NonNull final TreeParameter metadata) {
        return QuestionnaireTreeParameterOutput.builder()
                .value(metadata.getId().toString())
                .labels(metadata.getLabel().getPhrases())
                .children(metadata.getChildren().stream()
                        .map(this::from)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .answered(false)
                .build();
    }

    @NonNull
    private QuestionnaireParameterChildOutput from(@NonNull final ParameterChild child) {
        if (child instanceof ParameterItem) {
            return QuestionnaireParameterItemOutput.builder()
                    .value(child.getId().toString())
                    .labels(child.getLabel().getPhrases())
                    .build();
        } else if (child instanceof TreeParameter) {
            return from((TreeParameter) child);
        } else {
            throw new SurveyException("Invalid type");
        }
    }
}
