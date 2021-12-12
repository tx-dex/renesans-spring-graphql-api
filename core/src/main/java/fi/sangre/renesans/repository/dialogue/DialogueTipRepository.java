package fi.sangre.renesans.repository.dialogue;

import fi.sangre.renesans.persistence.dialogue.model.DialogueTipEntity;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;
import java.util.UUID;

public interface DialogueTipRepository extends JpaRepository<DialogueTipEntity, UUID>, QuerydslPredicateExecutor<DialogueTipEntity> {
    List<DialogueTipEntity> findAllByTopic(DialogueTopicEntity dialogueTopicEntity);
}
