package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RespondentGroupRepository extends JpaRepository<RespondentGroup, String> {
    Sort DEFAULT_RESPONDENT_GROUP_SORTING = new Sort(
            new Sort.Order(Sort.Direction.ASC, "title").ignoreCase(),
            new Sort.Order(Sort.Direction.ASC, "description").ignoreCase());

    RespondentGroup findDefaultRespondentBySurveyAndIsDefaultTrue(Survey survey);

    Boolean existsByCustomerAndIsDefaultTrue(Customer customer);

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<RespondentGroup> findBySurvey(Survey survey, Sort sort);

    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<RespondentGroup> findBySurvey(Survey survey) {
        return findBySurvey(survey, DEFAULT_RESPONDENT_GROUP_SORTING);
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<RespondentGroup> findByCustomer(Customer customer, Sort sort);

    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<RespondentGroup> findByCustomer(Customer customer) {
        return findByCustomer(customer, DEFAULT_RESPONDENT_GROUP_SORTING);
    }

    @PostAuthorize("hasPermission(returnObject, 'READ')")
    RespondentGroup findByRespondentsContaining(Respondent respondent);

    Set<RespondentGroup> findAllByRespondentsIn(Set<Respondent> respondents);

    // TODO rethink. used to return object for respondent submit
    Optional<RespondentGroup> findById(String id);

    // TODO rethink, user to return object for respondent invitation survey
    @Query("SELECT g FROM RespondentGroup g, Respondent r WHERE r.respondentGroup.id = g.id AND r.id = :id")
    RespondentGroup findByRespondentId(@Param("id") String id);
    
    @PreAuthorize("hasPermission(#customer, 'READ')")
    Long countByCustomer(@P("customer") Customer customer);
}


