package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.OrganizationInput;
import fi.sangre.renesans.graphql.input.SurveyInput;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;
import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationService {
    private final CustomerRepository customerRepository;
    private final QuestionService questionService;
    private final SegmentRepository segmentRepository;
    private final SurveyService  surveyService;
    private final SurveyRepository surveyRepository;
    private final MultilingualService multilingualService;

    @Transactional
    @CacheEvict(cacheNames = AUTH_CUSTOMER_IDS_CACHE, allEntries = true, condition = "#input.id == null")
    public Organization storeOrganization(@NonNull final OrganizationInput input) {

        final Customer customer;
        if (input.getId() != null) {
            customer = getByIdOrThrow(input.getId());

        } else {
            customer = Customer.builder().build();
        }

        if (input.getName() != null) {
            customer.setName(input.getName());
        }
        if (input.getDescription() != null) {
            customer.setDescription(input.getDescription());
        }

        customerRepository.save(customer);

        return Organization.builder()
                .id(customer.getId())
                .name(customer.getName())
                .description(customer.getDescription())
                .build();
    }

    @NonNull
    @Transactional
    public OrganizationSurvey storeSurvey(@NonNull final UUID organizationId, @NonNull final SurveyInput input, @NonNull final String languageTag) {
        final Customer customer = getByIdOrThrow(organizationId);
        final Survey survey;

        if (input.getId() == null) { // create
            survey = createOrganisationSurvey(customer, input, surveyService.getDefaultSurvey(), languageTag);

            customer.getSurveys().add(survey);
            customerRepository.save(customer);
        } else { // update
            checkArgument(input.getVersion() != null, "input.version cannot be null");
            survey = surveyRepository.findById(input.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));
            survey.setVersion(input.getVersion() + 1);

            surveyRepository.save(survey);
        }

        return toOrganizationSurvey(survey);
    }

    @NonNull
    @Transactional
    @CacheEvict(cacheNames = AUTH_CUSTOMER_IDS_CACHE, allEntries = true, condition = "#id == null")
    public Organization softDeleteOrganization(@NonNull final UUID id) {
        final Customer customer = getByIdOrThrow(id);

        customerRepository.delete(customer);

        return toOrganisation(customer);
    }

    @Nullable
    @Transactional(readOnly = true)
    public Segment getSegment(@NonNull final Organization organization) {
        final Customer customer = customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        return segmentRepository.findByCustomers(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Organization findOrganization(@NonNull final UUID id) {
        return toOrganisation(getByIdOrThrow(id));
    }

    @NonNull
    @Transactional(readOnly = true)
    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<Organization> findAll() {
        return toOrganisations(customerRepository.findAll());
    }

    @NonNull
    @Transactional(readOnly = true)
    public List<Organization> findAllBySegment(@NonNull final Segment segment) {
        return findAllBySegment(segment,this::toOrganisation);
    }

    @NonNull
    @Transactional(readOnly = true)
    public <T> List<T> findAllBySegment(@NonNull final Segment segment, @NonNull final Function<Customer, T> mapper) {
        return customerRepository.findAllBySegment(segment)
                .stream().map(mapper)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Long countBySegment(@NonNull final Segment segment) {
        return customerRepository.countBySegment(segment);
    }

    @NonNull
    @Transactional(readOnly = true)
//    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<OrganizationSurvey> getSurveys(@NonNull final Organization organization, @NonNull final String languageTag) {
        return customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"))
                .getSurveys().stream()
                .map(this::toOrganizationSurvey)
                .sorted((e1,e2) -> compare(e1.getMetadata().getTitles(), e2.getMetadata().getTitles(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private Survey createOrganisationSurvey(@NonNull final Customer customer, SurveyInput input, @NonNull final Survey defaultSurvey, @NonNull final String languageTag) {
        final SurveyMetadata.SurveyMetadataBuilder metadata = SurveyMetadata.builder();
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

        metadata.titles(ImmutableMap.of("en", "Organisation Survey"))
                .descriptions(ImmutableMap.of("en", "Organisation default survey"))
                .catalysts(catalysts.build());

        return surveyRepository.save(Survey.builder()
                .isDefault(false)
                .metadata(metadata.build())
                .build());
    }

    private Customer getByIdOrThrow(@NonNull final UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    @NonNull
    private List<Organization> toOrganisations(@NonNull final List<Customer> customers) {
        return customers.stream().map(this::toOrganisation)
                .collect(toList());
    }

    @NonNull
    private Organization toOrganisation(@NonNull final Customer customer) {
        return Organization.builder()
                .id(customer.getId())
                .name(customer.getName())
                .description(customer.getDescription())
                .build();
    }

    private OrganizationSurvey toOrganizationSurvey(@NonNull final Survey survey) {
        return OrganizationSurvey.builder()
                .id(survey.getId())
                .version(survey.getVersion())
                .metadata(survey.getMetadata())
                .build();
    }
}
