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
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyParameterOutputAssembler {
    @NonNull
    public List<SurveyParameterOutput> from(@Nullable final List<Parameter> parameters) {
        final List<SurveyParameterOutput> output = Optional.ofNullable(parameters)
                .orElse(ImmutableList.of())
                .stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        log.trace("Assembled output parameters: {}", output);

        return output;
    }

    @NonNull
    private SurveyParameterOutput from(@NonNull final Parameter parameter) {
        if (parameter instanceof ListParameter) {
            return from((ListParameter) parameter);
        } else if (parameter instanceof TreeParameter) {
            return from((TreeParameter) parameter);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private SurveyListParameterOutput from(@NonNull final ListParameter metadata) {
        return SurveyListParameterOutput.builder()
                .value(metadata.getId().toString())
                .labels(metadata.getLabel().getPhrases())
                .children(metadata.getValues().stream()
                        .map(v -> new SurveyParameterItemOutput(v.getId().toString(), v.getLabel().getPhrases()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private SurveyTreeParameterOutput from(@NonNull final TreeParameter metadata) {
        return SurveyTreeParameterOutput.builder()
                .value(metadata.getId().toString())
                .labels(metadata.getLabel().getPhrases())
                .children(metadata.getChildren().stream()
                        .map(this::from)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private SurveyParameterChildOutput from(@NonNull final ParameterChild child) {
        if (child instanceof ParameterItem) {
            return SurveyParameterItemOutput.builder()
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
