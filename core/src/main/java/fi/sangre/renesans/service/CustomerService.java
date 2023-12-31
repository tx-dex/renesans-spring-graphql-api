package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.DriverWeightInput;
import fi.sangre.renesans.model.CustomerDriverWeights;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.repository.CustomerDriverWeightsRepository;
import fi.sangre.renesans.repository.SegmentRepository;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Service
@Transactional
@CacheConfig(cacheManager = "authorizationCacheManager")
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final SegmentRepository segmentRepository;
    private final CustomerDriverWeightsRepository customerDriverWeightsRepository;

    public List<Customer> getAllCustomers() {
        return ImmutableList.copyOf(customerRepository.findAll());
    }

    public Customer getCustomer(final UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
    }

    @Transactional(readOnly = true)
    public Segment getCustomerSegment(@NonNull final Customer customer) {
        return segmentRepository.findByCustomers(customer).orElse(null);
    }

    @Deprecated
    public Customer storeCustomerDriverWeights(UUID customerId, List<DriverWeightInput> driverWeights) { // TODO why returning customer here? not list of weights
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

