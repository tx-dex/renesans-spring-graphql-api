package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.dto.DriverDto;
import fi.sangre.renesans.exception.CustomerNotFoundException;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.OrganizationInput;
import fi.sangre.renesans.model.Customer;
import fi.sangre.renesans.model.CustomerDriverWeights;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.repository.CustomerDriverWeightsRepository;
import fi.sangre.renesans.repository.CustomerRepository;
import fi.sangre.renesans.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;

@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationService {
    private final CustomerRepository customerRepository;
    private final QuestionService questionService;
    private final SegmentRepository segmentRepository;
    private final SurveyService  surveyService;
    private final CustomerDriverWeightsRepository customerDriverWeightsRepository;

    @Transactional
    @CacheEvict(cacheNames = AUTH_CUSTOMER_IDS_CACHE, allEntries = true, condition = "#input.id == null")
    public Organization storeOrganization(@NonNull final OrganizationInput input) {

        final Customer customer;
        if (input.getId() != null) {
            customer = customerRepository.findById(input.getId())
                    .orElseThrow(() -> new CustomerNotFoundException(input.getId()));

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

        final Customer savedCustomer = customerRepository.save(customer);

        if (input.getId() == null) {
            createDefaultCustomerDriverWeights(savedCustomer);
        }

        return Organization.builder()
                .id(customer.getId())
                .name(customer.getName())
                .description(customer.getDescription())
                .build();
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
    public OrganizationSurvey getSurvey(@NonNull final Organization organization) {
        final Survey survey = customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found", organization.getId())).getSurvey();
        return OrganizationSurvey.builder()
                .id(UUID.fromString(survey.getId()))
                .version(1L)
                .metadata(survey.getMetadata())
                .build();
    }

    private void createOrganisationSurvey(@NonNull final Customer customer, @NonNull final Survey defaultSurvey) {
        final Survey organizationSurvey = Survey.builder()
                .isDefault(false)
                .metadata(SurveyMetadata.builder()
                        .titles(ImmutableMap.of("en", "Organisation Survey"))
                        .descriptions(ImmutableMap.of("en", "Organisation default survey"))
                        .build())
                .build();

        customer.setSurvey(organizationSurvey);
    }

    private List<CustomerDriverWeights> createDefaultCustomerDriverWeights(final Customer customer) {

        final List<CustomerDriverWeights> weights = Lists.newArrayList();
        final List<DriverDto> drivers = questionService.getAllDrivers();

        drivers.forEach(driver ->
                weights.add(CustomerDriverWeights.builder()
                        .customerId(customer.getId())
                        .driverId(driver.getId())
                        .build())
        );
        return customerDriverWeightsRepository.saveAll(weights);
    }

}
