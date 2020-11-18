package fi.sangre.renesans.persistence.assemble;

import fi.sangre.renesans.application.model.parameter.*;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.metadata.parameters.*;
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
public class ParameterMetadataAssembler {
    @Nullable
    public List<ParameterMetadata> from(@NonNull final List<Parameter> parameters) {
        return parameters.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private ParameterMetadata from(@NonNull final Parameter parameter) {
        if (parameter instanceof ListParameter) {
            return from((ListParameter) parameter);
        } else if (parameter instanceof TreeParameter) {
            return from((TreeParameter) parameter);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private ParameterMetadata from(@NonNull final ListParameter parameter) {
        return ListParameterMetadata.builder()
                .id(parameter.getId())
                .titles(parameter.getLabel().getPhrases())
                .values(parameter.getValues().stream()
                        .map(v -> ParameterItemMetadata.builder()
                                .id(v.getId())
                                .titles(v.getLabel().getPhrases())
                                .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private ParameterMetadata from(@NonNull final TreeParameter parameter) {
        return TreeParameterMetadata.builder()
                .id(parameter.getId())
                .titles(parameter.getLabel().getPhrases())
                .children(parameter.getChildren().stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private ParameterChildMetadata from(@NonNull final ParameterChild child) {
        if (child instanceof ParameterItem) {
            return ParameterItemMetadata.builder()
                    .id(child.getId())
                    .titles(child.getLabel().getPhrases())
                    .build();
        } else if (child instanceof TreeParameter) {
            return (ParameterChildMetadata) from((TreeParameter) child);
        } else {
            throw new SurveyException("Invalid type");
        }
    }
}
