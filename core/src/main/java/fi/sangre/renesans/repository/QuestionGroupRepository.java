package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Survey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface QuestionGroupRepository extends JpaRepository<QuestionGroup,Long> {

    Optional<QuestionGroup> findByIdAndParentIsNull(Long id);
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<QuestionGroup> findAllByParentIsNull();

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<QuestionGroup> findAllByParentIsNotNull();


    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<QuestionGroup> findBySurveyAndParentIsNull(Survey survey);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<QuestionGroup> findByParent(QuestionGroup questionGroup);

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<QuestionGroup> findByParentId(Long catalystId);

    List<QuestionGroup> findByRespondentGroupsContaining(RespondentGroup respondentGroup);
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    QuestionGroup findByChildren(QuestionGroup questionGroup);
    List<QuestionGroup> findByIdInAndParentIsNull(List<Long> ids);
}


