package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import fi.sangre.renesans.exception.CustomerNotFoundException;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.DriverWeightInput;
import fi.sangre.renesans.model.Customer;
import fi.sangre.renesans.model.CustomerDriverWeights;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.repository.CustomerDriverWeightsRepository;
import fi.sangre.renesans.repository.CustomerRepository;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.SegmentRepository;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@Slf4j

@Service
@Transactional
@CacheConfig(cacheManager = "authorizationCacheManager")
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final SegmentRepository segmentRepository;
    private final SegmentService segmentService;
    private final CustomerDriverWeightsRepository customerDriverWeightsRepository;
    private final QuestionService questionService;

    @Autowired
    public CustomerService(
            final CustomerRepository customerRepository,
            final RespondentGroupRepository respondentGroupRepository,
            final SegmentRepository segmentRepository,
            final SegmentService segmentService,
            final CustomerDriverWeightsRepository customerDriverWeightsRepository,
            final QuestionService questionService
    ) {
        this.customerRepository = customerRepository;
        this.respondentGroupRepository = respondentGroupRepository;
        this.segmentRepository = segmentRepository;
        this.segmentService = segmentService;
        this.customerDriverWeightsRepository = customerDriverWeightsRepository;
        this.questionService = questionService;
    }

    public List<Customer> getAllCustomers() {
        return ImmutableList.copyOf(customerRepository.findAll());
    }

    public Customer getCustomer(final Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));
    }

    @CacheEvict(cacheNames = AUTH_CUSTOMER_IDS_CACHE, allEntries = true)
    public Customer deleteCustomer(Long id) {
        Customer customer = getCustomer(id);

        if (respondentGroupRepository.existsByCustomerAndIsDefaultTrue(customer)) {
            throw new GraphQLException("Can't remove a customer that has a default respondent group");
        }

        customerRepository.delete(customer);

        return customer;
    }

    @Transactional(readOnly = true)
    public Segment getCustomerSegment(@NonNull final Customer customer) {
        return segmentRepository.findByCustomers(customer).orElse(null);
    }

    public List<Customer> getCustomersAssignedToSegment(final Segment segment) {
        return customerRepository.findAllBySegment(segment);
    }

    public Long countCustomersForSegment(final Segment segment) {
        return Optional.ofNullable(customerRepository.countBySegment(segment)).orElse(0L);
    }

    public Customer getCustomer(RespondentGroup respondentGroup) {
        return customerRepository.findByGroupsContaining(respondentGroup);
    }

    public Customer storeCustomerDriverWeights(Long customerId, List<DriverWeightInput> driverWeights) { // TODO why returning customer here? not list of weights
        checkArgument(customerId != null, "Customer Id is required");

        final Customer customer = getCustomer(customerId);
        final List<CustomerDriverWeights> weightsToSave = Lists.newArrayList();

        final Map<Long, CustomerDriverWeights> weights = customerDriverWeightsRepository.findAllByCustomerId(customerId).stream()
                .collect(collectingAndThen(toMap(
                        CustomerDriverWeights::getDriverId,
                        e -> e
                ), Collections::unmodifiableMap));

        for (final DriverWeightInput driverWeight : driverWeights) {
            final CustomerDriverWeights weightToSave = weights.get(driverWeight.getId());
            if (weightToSave == null) {
                throw new ResourceNotFoundException("CustomerDriverWeight not found", driverWeight.getId());
            }

            final Double weight = driverWeight.getWeight();
            if (weight != null) {
                if (weight < 0 || weight > 1) {
                    throw new GraphQLException("Weight Value is not correct! must be between 0 and 1");
                }
                weightToSave.setWeight(weight);

                weightsToSave.add(weightToSave);
            }
        }
        customerDriverWeightsRepository.saveAll(weightsToSave);

        return customer;
    }
}

