package fi.sangre.renesans.repository.dialogue;

import fi.sangre.renesans.persistence.dialogue.model.DialogueCommentEntity;
import fi.sangre.renesans.persistence.dialogue.model.DialogueCommentLikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DialogueCommentLikeRepository extends JpaRepository<DialogueCommentLikeEntity, UUID>, QuerydslPredicateExecutor<DialogueCommentLikeEntity> {
    int countDialogueCommentLikeEntitiesByCommentEquals(DialogueCommentEntity commentEntity);

    int countDialogueCommentLikeEntitiesByCommentEqualsAndRespondentIdEquals(
            DialogueCommentEntity commentEntity,
            UUID respondentId
    );

    int countAllBySurveyId(UUID surveyId);
}