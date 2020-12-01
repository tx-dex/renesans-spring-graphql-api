package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.parameter.*;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.parameter.*;
import fi.sangre.renesans.persistence.model.metadata.parameters.*;
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
public class ParameterAssembler {
    @NonNull
    public List<Parameter> fromInputs(@NonNull final List<SurveyParameterInput> inputs, @NonNull final String languageTag) {
        if (new HashSet<>(inputs).size() != inputs.size()) {
            throw new SurveyException("Duplicated parameters' keys in the input");
        }

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
                .id(ParameterId.from(input.getValue()))
                .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                .values(input.getChildren().stream()
                        .map(e -> from(e, languageTag))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }

    @NonNull
    private ParameterItem from(@NonNull final SurveyParameterItemInput input, @NonNull final String languageTag) {
        return ParameterItem.builder()
                .id(ParameterId.from(input.getValue()))
                .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                .build();
    }

    @NonNull
    private Parameter from(@NonNull final SurveyTreeParameterInput input, @NonNull final String languageTag) {
        final TreeParameter.TreeParameterBuilder parameter = TreeParameter.builder()
                .id(ParameterId.from(input.getValue()))
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
                    .id(ParameterId.from(input.getValue()))
                    .label(new MultilingualText(ImmutableMap.of(languageTag, input.getLabel())))
                    .build();
        } else {
            return TreeParameter.builder()
                    .id(ParameterId.from(input.getValue()))
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
            return from((ListParameterMetadata) metadata, null, null);
        } else if (metadata instanceof TreeParameterMetadata) {
            return from((TreeParameterMetadata) metadata, null, null);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }

    @NonNull
    private Parameter from(@NonNull final ListParameterMetadata metadata, @Nullable final Parameter root, @Nullable final Parameter parent) {
        final ListParameter parameter = ListParameter.builder()
                .id(new ParameterId(metadata.getId()))
                .label(new MultilingualText(metadata.getTitles()))
                .build();

        parameter.setValues(metadata.getValues().stream()
                .map(e -> from(e, root != null ? root : parameter, parameter))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList)));

        return parameter;
    }

    @NonNull
    private ParameterItem from(@NonNull final ParameterItemMetadata metadata, @Nullable final Parameter root, @Nullable final Parameter parent) {
        return ParameterItem.builder()
                .root(root)
                .parent(parent)
                .id(new ParameterId(metadata.getId()))
                .label(new MultilingualText(metadata.getTitles()))
                .build();
    }

    @NonNull
    private Parameter from(@NonNull final TreeParameterMetadata metadata, @Nullable final Parameter root, @Nullable final Parameter parent) {
        final TreeParameter parameter = TreeParameter.builder()
                .root(root)
                .parent(parent)
                .id(new ParameterId(metadata.getId()))
                .label(new MultilingualText(metadata.getTitles()))
                .build();

        final List<ParameterChildMetadata> children = metadata.getChildren();
        if (children != null && !children.isEmpty()) {
            parameter.setChildren(children.stream()
                    .map(e -> from(e, root != null ? root : parameter, parameter))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList)));
        }

        return parameter;
    }

    @NonNull
    private ParameterChild from(@NonNull final ParameterChildMetadata metadata, @Nullable final Parameter root, @Nullable final Parameter parent) {
        if (metadata instanceof ParameterItemMetadata) {
            return from((ParameterItemMetadata) metadata, root, parent);
        } else if (metadata instanceof TreeParameterMetadata) {
            return (ParameterChild) from((TreeParameterMetadata) metadata, root, parent);
        } else {
            throw new SurveyException("Invalid parameter type");
        }
    }
}
