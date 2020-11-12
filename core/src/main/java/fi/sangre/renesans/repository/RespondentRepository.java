package fi.sangre.renesans.repository;


import com.querydsl.core.types.Predicate;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Customer;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface RespondentRepository extends JpaRepository<Respondent, String>, QuerydslPredicateExecutor<Respondent> {
    Sort DEFAULT_RESPONDENT_SORTING = new Sort(new Sort.Order(Sort.Direction.ASC, "name").ignoreCase());

    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<Respondent> findByRespondentGroup(RespondentGroup respondentGroup) {
        return findByRespondentGroup(respondentGroup, DEFAULT_RESPONDENT_SORTING);
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Respondent> findByRespondentGroup(RespondentGroup respondentGroup, Sort sort);

    @PostFilter("hasPermission(filterObject, 'READ')")
    default List<Respondent> findByRespondentGroupAndState(RespondentGroup respondentGroup, Respondent.State state) {
        return findByRespondentGroupAndState(respondentGroup, state, DEFAULT_RESPONDENT_SORTING);
    }

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Respondent> findByRespondentGroupAndState(RespondentGroup respondentGroup, Respondent.State state, Sort sort);

    @PostFilter("hasPermission(filterObject, 'READ')")
    List<Respondent> findAll(Sort sort);

    @Override
    default List<Respondent> findAll() {
        return findAll(DEFAULT_RESPONDENT_SORTING);
    }

    List<Respondent> findAll(Predicate predicate, Sort sort);

    @Override
    default List<Respondent> findAll(Predicate predicate) {
        return findAll(predicate, DEFAULT_RESPONDENT_SORTING);
    }

    @Override
    @PreFilter("hasPermission(filterObject, 'WRITE')")
    void deleteAll(Iterable<? extends Respondent> respondents);

    Respondent findByInvitationHash(String invitationHash);

    // to be used only for invitation queries
    Optional<Respondent> findById(String respondentId);

    Set<Respondent> findAllByIdIn(Set<String> ids);

    @PreAuthorize("hasPermission(#customer, 'READ')")
    Long countByStateAndRespondentGroup_Customer(Respondent.State state, Customer customer);

    @PreAuthorize("hasPermission(#customer, 'READ')")
    default Long countByRespondentGroup_Customer(Customer customer) {
        return countByStateAndRespondentGroup_Customer(Respondent.State.FINISHED, customer);
    }

    @PreAuthorize("hasPermission(#respondentGroup, 'READ')")
    Long countByStateAndRespondentGroup(Respondent.State state, RespondentGroup respondentGroup);

    @PreAuthorize("hasPermission(#respondentGroup, 'READ')")
    default Long countByRespondentGroup(RespondentGroup respondentGroup) {
        return countByStateAndRespondentGroup(Respondent.State.FINISHED, respondentGroup);
    }

    @Query(value = "select r from Respondent r where r.respondentGroupId = :groupId and (r.id = :originalId or r.originalId = :originalId)")
    Optional<Respondent> findOriginalRespondent(
            @Param("groupId")    String groupId,
            @Param("originalId") String originalId);
}


