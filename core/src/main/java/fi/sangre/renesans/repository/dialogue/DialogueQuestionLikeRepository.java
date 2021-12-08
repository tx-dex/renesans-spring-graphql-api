package fi.sangre.renesans.repository.dialogue;

import fi.sangre.renesans.persistence.dialogue.model.DialogueQuestionLikeEntity;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicQuestionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DialogueQuestionLikeRepository extends JpaRepository<DialogueQuestionLikeEntity, UUID>, QuerydslPredicateExecutor<DialogueQuestionLikeEntity> {
    int countDialogueQuestionLikeEntitiesByQuestionEquals(DialogueTopicQuestionEntity questionEntity);

    int countDialogueQuestionLikeEntitiesByQuestionEqualsAndRespondentIdEquals(
            DialogueTopicQuestionEntity questionEntity,
            UUID respondentId
    );

    int countAllBySurveyId(UUID surveyId);
}
