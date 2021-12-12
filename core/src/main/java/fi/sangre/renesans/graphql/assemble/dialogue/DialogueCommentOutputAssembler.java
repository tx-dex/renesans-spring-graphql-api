package fi.sangre.renesans.graphql.assemble.dialogue;

import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.graphql.output.dialogue.DialogueCommentOutput;
import fi.sangre.renesans.persistence.dialogue.model.DialogueCommentEntity;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import fi.sangre.renesans.repository.dialogue.DialogueCommentLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class DialogueCommentOutputAssembler {
    @Autowired
    private DialogueCommentLikeRepository dialogueCommentLikeRepository;

    @NonNull
    public DialogueCommentOutput from(DialogueCommentEntity entity, RespondentId viewingRespondentId) {
        boolean hasLikeByThisRespondent = dialogueCommentLikeRepository
                .countDialogueCommentLikeEntitiesByCommentEqualsAndRespondentIdEquals(
                        entity, viewingRespondentId.getValue()
                ) > 0;

        int likesCount = dialogueCommentLikeRepository.countDialogueCommentLikeEntitiesByCommentEquals(entity);
        UUID commentAuthorId = entity.getRespondent().getId();

        return DialogueCommentOutput.builder()
                .id(entity.getId())
                .respondentColor(entity.getRespondent().getColor())
                .replies(from(entity.getReplies(), viewingRespondentId))
                .likesCount(likesCount)
                .hasLikeByThisRespondent(hasLikeByThisRespondent)
                .isOwnedByThisRespondent(commentAuthorId.equals(viewingRespondentId.getValue()))
                .authorRespondentId(commentAuthorId)
                .text(entity.getText())
                .createdAt(entity.getCreatedOn().toString())
                .build();
    }

    @NonNull
    public Collection<DialogueCommentOutput> from(
            Collection<DialogueCommentEntity> entityList, RespondentId respondentId) {
        Collection<DialogueCommentOutput> outputs = new ArrayList<>();

        entityList.forEach(entity -> {
            outputs.add(from(entity, respondentId));
        });

        return outputs;
    }
}
