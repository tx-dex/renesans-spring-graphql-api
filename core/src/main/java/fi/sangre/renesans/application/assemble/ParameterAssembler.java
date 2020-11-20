package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.parameter.*;
import fi.sangre.renesans.application.utils.UUIDUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.parameter.*;
import fi.sangre.renesans.persistence.model.metadata.parameters.*;
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
public class ParameterAssembler {

    @NonNull
    public List<Parameter> fromInputs(@NonNull final List<SurveyParameterInput> inputs, @NonNull final String languageTag) {
        return inputs.stream()
                .map(e -> from(e, languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public Parameter from(@NonNull final SurveyParameterInput input, @NonNull final String languageTag) {
        if (input instanceof SurveyListParameterInput) {
            return from((SurveyListParameterInput) input, languageTag);
        } else if (input instanceof SurveyTreeParameterInput) {
            return from((SurveyTreeParameterInput) input, languageTag);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private Parameter from(@NonNull final SurveyListParameterInput input, @NonNull final String languageTag) {
        return ListParameter.builder()
                .id(UUIDUtils.from(input.getValue()))
                .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                .values(input.getChildren().stream()
                        .map(e -> from(e, languageTag))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private ParameterItem from(@NonNull final SurveyParameterItemInput input, @NonNull final String languageTag) {
        return ParameterItem.builder()
                .id(UUIDUtils.from(input.getValue()))
                .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                .build();
    }

    @NonNull
    private Parameter from(@NonNull final SurveyTreeParameterInput input, @NonNull final String languageTag) {
        final TreeParameter.TreeParameterBuilder parameter = TreeParameter.builder()
                .id(UUIDUtils.from(input.getValue()))
                .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())));

        final List<SurveyTreeParameterChildInput> children = input.getChildren();
        if (children != null && !children.isEmpty()) {
            parameter.children(children.stream()
                    .map(e -> from(e, languageTag))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList)));
        }

        return parameter.build();
    }

    @NonNull
    private ParameterChild from(@NonNull final SurveyTreeParameterChildInput input, @NonNull final String languageTag) {
        if (input.getChildren() == null) {
            return ParameterItem.builder()
                    .id(UUIDUtils.from(input.getValue()))
                    .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                    .build();
        } else {
            return TreeParameter.builder()
                    .id(UUIDUtils.from(input.getValue()))
                    .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                    .children(input.getChildren().stream()
                            .map(e -> from(e, languageTag))
                            .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                    .build();
        }
    }

    @NonNull
    public List<Parameter> fromMetadata(@Nullable final List<ParameterMetadata> metadata) {
        return Optional.ofNullable(metadata)
                .orElse(ImmutableList.of())
                .stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private Parameter from(@NonNull final ParameterMetadata metadata) {
        if (metadata instanceof ListParameterMetadata) {
            return from((ListParameterMetadata) metadata);
        } else if (metadata instanceof TreeParameterMetadata) {
            return from((TreeParameterMetadata) metadata);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private Parameter from(@NonNull final ListParameterMetadata metadata) {
        return ListParameter.builder()
                .id(metadata.getId())
                .label(new MultilingualText(metadata.getTitles()))
                .values(metadata.getValues().stream()
                        .map(this::from)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private ParameterItem from(@NonNull final ParameterItemMetadata metadata) {
        return ParameterItem.builder()
                .id(metadata.getId())
                .label(new MultilingualText(metadata.getTitles()))
                .build();
    }

    @NonNull
    private Parameter from(@NonNull final TreeParameterMetadata metadata) {
        final TreeParameter.TreeParameterBuilder parameter = TreeParameter.builder()
                .id(metadata.getId())
                .label(new MultilingualText(metadata.getTitles()));

        final List<ParameterChildMetadata> children = metadata.getChildren();
        if (children != null && !children.isEmpty()) {
            parameter.children(children.stream()
                    .map(this::from)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList)));
        }

        return parameter.build();
    }

    @NonNull
    private ParameterChild from(@NonNull final ParameterChildMetadata metadata) {
        if (metadata instanceof ParameterItemMetadata) {
            return from((ParameterItemMetadata) metadata);
        } else if (metadata instanceof TreeParameterMetadata) {
            return (ParameterChild) from((TreeParameterMetadata) metadata);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }
}
