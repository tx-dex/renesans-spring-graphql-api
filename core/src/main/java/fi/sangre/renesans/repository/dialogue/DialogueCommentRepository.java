package fi.sangre.renesans.repository.dialogue;

import fi.sangre.renesans.persistence.dialogue.model.DialogueCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DialogueCommentRepository extends JpaRepository<DialogueCommentEntity, UUID>, QuerydslPredicateExecutor<DialogueCommentEntity> {
    int countAllBySurveyId(UUID surveyId);
}
