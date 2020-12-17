package fi.sangre.renesans.persistence.repository;

import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.OrganizationSurveyMapping;
import fi.sangre.renesans.persistence.model.SurveyStateCounters;
import fi.sangre.renesans.persistence.model.User;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Sort DEFAULT_CUSTOMER_SORTING = new Sort(new Sort.Order(Sort.Direction.ASC, "name").ignoreCase());

    @Query("SELECT new fi.sangre.renesans.persistence.model.OrganizationSurveyMapping(o.id, s.id) " +
            "FROM Survey s " +
            "INNER JOIN s.organisations o")
    List<OrganizationSurveyMapping> getOrganizationSurveyMappings();

    @Query("SELECT new fi.sangre.renesans.persistence.model.SurveyStateCounters(o.id, count(s)) " +
            "FROM Customer o " +
            "LEFT JOIN o.surveys s " +
            "GROUP BY o.id")
    List<SurveyStateCounters> countOrganizationSurveys();

    @NonNull
    @Override
    default List<Customer> findAll() {
        return findAll(DEFAULT_CUSTOMER_SORTING);
    }

    @NonNull
    List<Customer> findAll(@NonNull Sort sort);

    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<Customer> findAllBySegment(@NonNull final Segment segment) {
        return findAllBySegment(segment, DEFAULT_CUSTOMER_SORTING);
    }

    @NonNull
    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Customer> findAllBySegment(@NonNull Segment segment, @NonNull Sort sort);

    @NonNull
    Long countBySegment(Segment segment);

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    Customer findByGroupsContaining(RespondentGroup respondentGroup);

    @Deprecated
    List<Customer> findAllByGroupsIdIn(Set<String> respondentGroupIds);
    Set<Customer> findByUsersContaining(User user);
    @NonNull
    List<Customer> findByUsersContainingOrCreatedBy(@NonNull User user, @NonNull Long createdBy);
}


