package fi.sangre.renesans.service;

import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.ParameterAssembler;
import fi.sangre.renesans.application.merge.ParameterMerger;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.persistence.assemble.ParameterMetadataAssembler;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationSurveyService {
    private final SurveyRepository surveyRepository;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final CustomerRepository customerRepository;
    private final ParameterAssembler parameterAssembler;
    private final ParameterMetadataAssembler parameterMetadataAssembler;
    private final ParameterMerger parameterMerger;

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey getSurvey(@NonNull final UUID id) {
        return organizationSurveyAssembler.from(surveyRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Survey not found")));
    }

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

        final List<Parameter> existing = parameterAssembler.fromMetadata(metadata.getParameters());
        final List<Parameter> inputs = parameterAssembler.fromInputs(input, languageTag);
        final List<Parameter> combined = parameterMerger.combine(existing, inputs);
        metadata.setParameters(parameterMetadataAssembler.from(combined));

        return organizationSurveyAssembler.from(surveyRepository.save(survey));
    }


}
