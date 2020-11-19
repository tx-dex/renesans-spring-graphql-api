package fi.sangre.renesans.service;

import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.OrganizationInput;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
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

import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationService {
    private final CustomerRepository customerRepository;
    private final SegmentRepository segmentRepository;

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
}
