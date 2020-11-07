package fi.sangre.renesans.repository;

import fi.sangre.renesans.model.Customer;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Segment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {
    Sort DEFAULT_QUESTION_SORTING = new Sort(new Sort.Order(Sort.Direction.ASC, "seq"));

    Optional<Question> findById(Long id);


    default List<Question> findAllBySegment(Segment segment) {
        return findAllBySegment(segment, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllBySegment(Segment segment, Sort sort);


    default List<Question> findAllByQuestionGroupIdAndSegment(Long catalystId, Segment segment) {
        return findAllByQuestionGroupIdAndSegment(catalystId, segment, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllByQuestionGroupIdAndSegment(Long catalystId, Segment segment, Sort sort);


    default List<Question> findAllByCustomer(Customer customer) {
        return findAllByCustomer(customer, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllByCustomer(Customer customer, Sort sort);


    default List<Question> findAllByQuestionGroupIdAndCustomer(Long catalystId, Customer customer) {
        return findAllByQuestionGroupIdAndCustomer(catalystId, customer, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllByQuestionGroupIdAndCustomer(Long catalystId, Customer customer, Sort sort);


    default List<Question> findAllByQuestionGroupIdAndIdIn(Long catalystId, Set<Long> questionIds) {
        return findAllByQuestionGroupIdAndIdIn(catalystId, questionIds, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllByQuestionGroupIdAndIdIn(Long catalystId, Set<Long> questionIds, Sort sort);

    default List<Question> findAllBySourceTypeAndQuestionGroupId(Question.SourceType sourceType, Long catalystId) {
        return findAllBySourceTypeAndQuestionGroupId(sourceType, catalystId, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllBySourceTypeAndQuestionGroupId(Question.SourceType sourceType, Long catalystId, Sort sort);

    default List<Question> findAllBySourceType(Question.SourceType sourceType) {
        return findAllBySourceType(sourceType, DEFAULT_QUESTION_SORTING);
    }
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Question> findAllBySourceType(Question.SourceType sourceType, Sort sort);
}


