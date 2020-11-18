package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.output.parameter.*;
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
public class SurveyParameterOutputAssembler {
    @NonNull
    public List<SurveyParameterOutput> from(@Nullable final List<ParameterMetadata> metadata) {
        if (metadata == null) {
            return ImmutableList.of();
        } else {
            return metadata.stream()
                    .map(this::from)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        }
    }

    @NonNull
    private SurveyParameterOutput from(@NonNull final ParameterMetadata metadata) {
        if (metadata instanceof ListParameterMetadata) {
            return from((ListParameterMetadata) metadata);
        } else if (metadata instanceof TreeParameterMetadata) {
            return from((TreeParameterMetadata) metadata);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private SurveyListParameterOutput from(@NonNull final ListParameterMetadata metadata) {
        return SurveyListParameterOutput.builder()
                .value(metadata.getId().toString())
                .labels(metadata.getLabel().getPhrases())
                .children(metadata.getValues().stream()
                        .map(v -> new SurveyParameterValueOutput(v.getId().toString(), v.getLabel().getPhrases()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private SurveyTreeParameterOutput from(@NonNull final TreeParameterMetadata metadata) {
        return SurveyTreeParameterOutput.builder()
                .value(metadata.getId().toString())
                .labels(metadata.getLabel().getPhrases())
                .children(metadata.getChildren().stream()
                        .map(this::from)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private SurveyParameterChildOutput from(@NonNull final ParameterChildMetadata child) {
        if (child instanceof ParameterItemMetadata) {
            return SurveyParameterValueOutput.builder()
                    .value(child.getId().toString())
                    .labels(((ParameterItemMetadata) child).getLabel().getPhrases())
                    .build();
        } else if (child instanceof TreeParameterMetadata) {
            return null; //TODO: implement
        } else {
            throw new SurveyException("Invalid type");
        }
    }
}
