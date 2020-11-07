package fi.sangre.renesans.repository;


import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Weight;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeightRepository extends JpaRepository<Weight,Long> {
    Sort DEFAULT_WEIGHT_SORTING = new Sort(new Sort.Order(Sort.Direction.ASC, "questionGroupId").ignoreCase());

    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<Weight> findAllByQuestion(Question question, Sort sort);

    default List<Weight> findAllByQuestion(Question question) {
        return findAllByQuestion(question, DEFAULT_WEIGHT_SORTING);
    }

    Optional<Weight> findByQuestionIdAndQuestionGroupId(Long questionId, Long questionGroupId);
}


