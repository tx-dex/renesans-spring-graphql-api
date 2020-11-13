package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.persistence.model.Customer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    Sort DEFAULT_CUSTOMER_SORTING = new Sort(new Sort.Order(Sort.Direction.ASC, "name").ignoreCase());

    @NonNull
    @PostAuthorize("hasPermission(returnObject, 'READ')")
    Optional<Customer> findById(@P("id") @NonNull Long id);

    @NonNull
    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Customer> findAll(@NonNull Sort sort);

    @NonNull
    @Override
    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<Customer> findAll() {
        return findAll(DEFAULT_CUSTOMER_SORTING);
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Customer> findAllBySegment(final Segment segment, Sort sort);

    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<Customer> findAllBySegment(final Segment segment) {
        return findAllBySegment(segment, DEFAULT_CUSTOMER_SORTING);
    }

    @NonNull
    Long countBySegment(Segment segment);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    Customer findByGroupsContaining(RespondentGroup respondentGroup);

    List<Customer> findAllByGroupsIdIn(Set<String> respondentGroupIds);
    Set<Customer> findByUsersContaining(User user);
    List<Customer> findByUsersContainingOrCreatedBy(User user, Long createdBy);

}


