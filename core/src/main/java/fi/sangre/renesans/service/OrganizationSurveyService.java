package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.ParameterAssembler;
import fi.sangre.renesans.application.merge.ParameterMerger;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.SurveyInput;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.persistence.assemble.ParameterMetadataAssembler;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
import fi.sangre.renesans.persistence.model.metadata.LocalisationMetadata;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static fi.sangre.renesans.application.utils.MultilingualUtils.create;
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
    private final QuestionService questionService;
    private final MultilingualService multilingualService;

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
    public OrganizationSurvey storeSurvey(@NonNull final UUID organizationId, @NonNull final SurveyInput input, @NonNull final String languageTag) {
        final Customer customer = customerRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        final Survey survey;

        if (input.getId() == null) { // create
            survey = createSurvey(customer, input, surveyRepository.findByIsDefaultTrue().orElse(null), languageTag);

            customer.getSurveys().add(survey);
            customerRepository.save(customer);
        } else { // update
            checkArgument(input.getVersion() != null, "input.version cannot be null");
            survey = surveyRepository.findById(input.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

            surveyRepository.saveAndFlush(survey);
        }

        return organizationSurveyAssembler.from(survey);
    }

    @NonNull
    @Transactional
    public OrganizationSurvey storeSurveyParameters(@NonNull final UUID surveyId, @NonNull final Long surveyVersion, @NonNull final List<SurveyParameterInput> input, @NonNull final String languageTag) {
        final Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

        final SurveyMetadata metadata = copy(survey.getMetadata());

        final List<Parameter> existing = parameterAssembler.fromMetadata(metadata.getParameters());
        final List<Parameter> inputs = parameterAssembler.fromInputs(input, languageTag);
        final List<Parameter> combined = parameterMerger.combine(existing, inputs);
        metadata.setParameters(parameterMetadataAssembler.from(combined));
        survey.setMetadata(metadata);

        return organizationSurveyAssembler.from(surveyRepository.saveAndFlush(survey));
    }

    @NonNull
    @Transactional
    public OrganizationSurvey softDeleteSurvey(@NonNull final UUID surveyId) {
        final Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

        surveyRepository.delete(survey);

        final OrganizationSurvey organizationSurvey = organizationSurveyAssembler.from(survey);
        organizationSurvey.setVersion(survey.getVersion() + 1); // This only done so incremented version is returned to frontend
        organizationSurvey.setDeleted(true);

        return organizationSurvey;
    }

    @NonNull
    private Survey createSurvey(@NonNull final Customer customer, SurveyInput input, @Nullable final Survey defaultSurvey, @NonNull final String languageTag) {
        final SurveyMetadata.SurveyMetadataBuilder metadata = SurveyMetadata
                .builder()
                .titles(create(input.getTitle(), languageTag))
                .descriptions(create(input.getDescription(), languageTag))
                .localisation(LocalisationMetadata.builder().build());

        final ImmutableList.Builder<CatalystMetadata> catalysts = ImmutableList.builder();

        for (final CatalystDto catalyst : questionService.getCatalysts(customer)) {
            final List<DriverMetadata> drivers = questionService.getAllCatalystDrivers(catalyst.getId(), customer)
                    .stream()
                    .map(driver -> DriverMetadata.builder()
                            .id(driver.getId())
                            .pdfName(driver.getPdfName())
                            .titles(multilingualService.getPhrases(driver.getTitleId()))
                            .descriptions(multilingualService.getPhrases(driver.getDescriptionId()))
                            .prescriptions(multilingualService.getPhrases(driver.getPrescriptionId()))
                            .weight(driver.getWeight())
                            .build())

                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            catalysts.add(CatalystMetadata.builder()
                    .id(catalyst.getId())
                    .pdfName(catalyst.getPdfName())
                    .titles(multilingualService.getPhrases(catalyst.getTitleId()))
                    .weight(catalyst.getWeight())
                    .drivers(drivers)
                    .build());
        }

        //TODO: throw on required
        metadata.catalysts(catalysts.build());

        return surveyRepository.save(Survey.builder()
                .version(1L)
                .isDefault(false)
                .metadata(metadata.build())
                .build());
    }


    @NonNull
    private SurveyMetadata copy(@Nullable final SurveyMetadata metadata) {
        if (metadata == null) {
            return SurveyMetadata.builder().build();
        } else {
            return SurveyMetadata.builder()
                    .titles(metadata.getTitles())
                    .descriptions(metadata.getDescriptions())
                    .catalysts(metadata.getCatalysts())
                    .parameters(metadata.getParameters())
                    .localisation(metadata.getLocalisation())
                    .build();
        }
    }
}
