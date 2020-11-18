package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.parameter.*;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.model.metadata.parameters.*;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationSurveyService {
    private final SurveyRepository surveyRepository;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final CustomerRepository customerRepository;

    @NonNull
    @Transactional(readOnly = true)
//    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<OrganizationSurvey> getSurveys(@NonNull final Organization organization, @NonNull final String languageTag) {
        return customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"))
                .getSurveys().stream()
                .map(organizationSurveyAssembler::from)
                .sorted((e1,e2) -> compare(e1.getMetadata().getTitles(), e2.getMetadata().getTitles(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    @Transactional
    public OrganizationSurvey storeSurveyParameters(@NonNull final UUID surveyId, @NonNull final Long surveyVersion, @NonNull final List<SurveyParameterInput> input, @NonNull final String languageTag) {
        final Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

        final SurveyMetadata metadata = survey.getMetadata();

        metadata.setParameters(combine(metadata.getParameters(), input, languageTag));

        return organizationSurveyAssembler.from(surveyRepository.save(survey));
    }

    @NonNull
    private List<ParameterMetadata> combine(@Nullable final List<ParameterMetadata> parameters, @NonNull final List<SurveyParameterInput> input, @NonNull final String languageTag) {
        final ImmutableList.Builder<ParameterMetadata> combined = ImmutableList.builder();
        final Map<UUID, ParameterMetadata> existingParameters;
        if (parameters == null) {
            existingParameters = ImmutableMap.of();
        } else {
            existingParameters = parameters.stream()
                    .collect(collectingAndThen(toMap(ParameterMetadata::getId, v -> v), Collections::unmodifiableMap));
        }

        for(final SurveyParameterInput inputParameter : input) {
            if (inputParameter instanceof SurveyListParameterInput) {
                combined.add(combine(existingParameters, (SurveyListParameterInput) inputParameter, languageTag));
            } else if (inputParameter instanceof SurveyTreeParameterInput) {
                combined.add(combine(existingParameters, (SurveyTreeParameterInput) inputParameter, languageTag));
            } else {
                throw new SurveyException("Invalid input parameter type");
            }
        }

        return combined.build();
    }

    @NonNull
    private ParameterMetadata combine(@NonNull final Map<UUID, ParameterMetadata> existingParameters, @NonNull final SurveyListParameterInput input, @NonNull final String languageTag) {
        final UUID id;
        final ParameterMetadata existing;
        if (input.getValue() == null) {
            id = UUID.randomUUID();
            existing = ListParameterMetadata.builder().id(id).build();
        } else {
            id = UUID.fromString(input.getValue());
            existing = Optional.ofNullable(existingParameters.get(id))
                    .orElse(ListParameterMetadata.builder().id(id).build());
        }

        if (existing instanceof ListParameterMetadata) {
            return combine((ListParameterMetadata) existing, input, languageTag);
        } else {
            throw new SurveyException("Existing parameter is has a different type. Remove and add new parameter");
        }
    }

    @NonNull
    private ParameterMetadata combine(@NonNull final ListParameterMetadata existing, @NonNull final SurveyListParameterInput input, @NonNull final String languageTag) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel(), languageTag));

        final ImmutableList.Builder<ParameterItemMetadata> values = ImmutableList.builder();
        final Map<UUID, ParameterItemMetadata> existingItems = existing.getValues().stream()
                .collect(collectingAndThen(toMap(ParameterItemMetadata::getId, v -> v), Collections::unmodifiableMap));

        for (final SurveyParameterItemInput item : input.getChildren()) {
            values.add(combineItem(existingItems, item, languageTag));
        }

        existing.setValues(values.build());

        return existing;
    }

    @NonNull
    private ParameterItemMetadata combineItem(@NonNull final Map<UUID, ParameterItemMetadata> existingItems, @NonNull final SurveyParameterItemInput input, @NonNull final String languageTag) {
        final UUID id;
        final ParameterItemMetadata existing;
        if (input.getValue() == null) {
            id = UUID.randomUUID();
            existing = ParameterItemMetadata.builder().id(id).build();
        } else {
            id = UUID.fromString(input.getValue());
            existing = Optional.ofNullable(existingItems.get(id))
            .orElse(ParameterItemMetadata.builder().id(id).build());
        }

        return combineItem(existing, input, languageTag);
    }

    @NonNull
    private ParameterItemMetadata combineItem(@NonNull final ParameterItemMetadata existing, @NonNull final SurveyParameterItemInput input, @NonNull final String languageTag) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel(), languageTag));

        return existing;
    }

    @NonNull
    private ParameterItemMetadata combineItem(@NonNull final ParameterItemMetadata existing, @NonNull final SurveyTreeParameterChildInput input, @NonNull final String languageTag) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel(), languageTag));

        return existing;
    }

    @NonNull
    private ParameterMetadata combine(@NonNull final Map<UUID, ParameterMetadata> existingParameters, @NonNull final SurveyTreeParameterInput input, @NonNull final String languageTag) {
        final UUID id;
        final ParameterMetadata existing;
        if (input.getValue() == null) {
            id = UUID.randomUUID();
            existing = TreeParameterMetadata.builder().id(id).build();
        } else {
            id = UUID.fromString(input.getValue());
            existing = Optional.ofNullable(existingParameters.get(id))
                    .orElse(TreeParameterMetadata.builder().id(id).build());
        }

        if (existing instanceof TreeParameterMetadata) {
            return combine((TreeParameterMetadata) existing, input, languageTag);
        } else {
            throw new SurveyException("Existing parameter is has a different type. Remove and add new parameter");
        }
    }

    @NonNull
    private ParameterMetadata combine(@NonNull final TreeParameterMetadata existing, @NonNull final SurveyTreeParameterInput input, @NonNull final String languageTag) {
        existing.setLabel(MultilingualUtils.combine(existing.getLabel(), input.getLabel(), languageTag));

        final ImmutableList.Builder<ParameterChildMetadata> children = ImmutableList.builder();
        final Map<UUID, ParameterChildMetadata> existingItems = Optional.ofNullable(existing.getChildren())
                .orElse(ImmutableList.of()).stream()
                .collect(collectingAndThen(toMap(ParameterChildMetadata::getId, v -> v), Collections::unmodifiableMap));

        for (final SurveyTreeParameterChildInput item : input.getChildren()) {
            children.add(combineChild(existingItems, item, languageTag));
        }

        existing.setChildren(children.build());

        return existing;
    }

    @NonNull
    private ParameterChildMetadata combineChild(@NonNull final Map<UUID, ParameterChildMetadata> existingItems, @NonNull final SurveyTreeParameterChildInput input, @NonNull final String languageTag) {
        if (input.getChildren() == null) {
            final UUID id;
            final ParameterChildMetadata existing;
            if (input.getValue() == null) {
                id = UUID.randomUUID();
                existing = ParameterItemMetadata.builder().id(id).build();
            } else {
                id = UUID.fromString(input.getValue());
                existing = Optional.ofNullable(existingItems.get(id))
                        .orElse(ParameterItemMetadata.builder().id(id).build());
            }

            if (existing instanceof  ParameterItemMetadata) {
                return combineItem((ParameterItemMetadata) existing, input, languageTag);
            } else {
                return combineItem(ParameterItemMetadata.builder().id(id).build(), input, languageTag);
            }
        } else {
            //TODO: implement
            return null;
        }
    }
}
