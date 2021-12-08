package fi.sangre.renesans.repository.dialogue;

import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicEntity;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import javax.persistence.QueryHint;
import java.util.List;
import java.util.UUID;

@Repository
public interface DialogueTopicRepository extends JpaRepository<DialogueTopicEntity, UUID>, QuerydslPredicateExecutor<DialogueTopicEntity> {
    @QueryHints(value = {
            @QueryHint(name = "org.hibernate.cacheable", value = "true"),
    })
    List<DialogueTopicEntity> findAllBySurveyId(UUID surveyId, Sort sort);

    int countAllBySurveyIdAndActiveTrue(UUID surveyId);
}