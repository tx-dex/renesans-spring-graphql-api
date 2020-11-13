package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.OrganizationInput;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;
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
        if (input.getSegmentId() != null) {
            final Segment segment = segmentRepository.findById(input.getSegmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Segment not found", input.getId()));
            customer.setSegment(segment);
            log.debug("Assigning segment id:{} to customer: id: {} '{}'", segment.getId(), customer.getId(), customer.getName());
        }

        if (input.getId() == null) {
            createOrganisationSurvey(customer, surveyService.getDefaultSurvey());
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
    @CacheEvict(cacheNames = AUTH_CUSTOMER_IDS_CACHE, allEntries = true, condition = "#id == null")
    public Organization softDeleteOrganization(@NonNull final Long id) {
        final Customer customer = getByIdOrThrow(id);

        customerRepository.delete(customer);

        return toOrganisation(customer);
    }

    @Nullable
    @Transactional(readOnly = true)
    public Segment getSegment(@NonNull final Organization organization) {
        final Customer customer = customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found", organization.getId()));
        return segmentRepository.findByCustomers(customer)
                .orElseThrow(() -> new ResourceNotFoundException("Segment not found"));
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
    public OrganizationSurvey getSurvey(@NonNull final Organization organization) {
        final Survey survey = customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found", organization.getId())).getSurvey();
        return OrganizationSurvey.builder()
                .id(UUID.fromString(survey.getId()))
                .version(survey.getVersion())
                .metadata(survey.getMetadata())
                .build();
    }

    private void createOrganisationSurvey(@NonNull final Customer customer, @NonNull final Survey defaultSurvey) {
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

        final Survey organizationSurvey = Survey.builder()
                .isDefault(false)
                .metadata(metadata.build())
                .build();

        customer.setSurvey(organizationSurvey);
    }

    private Customer getByIdOrThrow(@NonNull final Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    private Organization toOrganisation(@NonNull final Customer customer) {
        return Organization.builder()
                .id(customer.getId())
                .name(customer.getName())
                .description(customer.getDescription())
                .build();
    }
}
