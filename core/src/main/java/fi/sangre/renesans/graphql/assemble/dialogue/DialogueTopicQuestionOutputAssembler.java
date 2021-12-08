package fi.sangre.renesans.graphql.assemble.dialogue;

import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.graphql.output.dialogue.DialogueQuestionOutput;
import fi.sangre.renesans.persistence.dialogue.model.DialogueCommentEntity;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicQuestionEntity;
import fi.sangre.renesans.repository.dialogue.DialogueQuestionLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class DialogueTopicQuestionOutputAssembler {
    @Autowired
    private DialogueCommentOutputAssembler dialogueCommentOutputAssembler;

    @Autowired
    private DialogueQuestionLikeRepository dialogueQuestionLikeRepository;

    @NonNull
    public DialogueQuestionOutput from(DialogueTopicQuestionEntity entity, RespondentId respondentId) {
        Map<UUID, DialogueCommentEntity> commentEntityMap = entity.getComments();

        boolean hasLikeByThisRespondent = dialogueQuestionLikeRepository
                .countDialogueQuestionLikeEntitiesByQuestionEqualsAndRespondentIdEquals(
                        entity, respondentId.getValue()
                ) > 0;

        int likesCount = dialogueQuestionLikeRepository.countDialogueQuestionLikeEntitiesByQuestionEquals(entity);

        return DialogueQuestionOutput.builder()
                .title(entity.getTitle())
                .active(entity.getActive())
                .sortOrder(entity.getSortOrder())
                .comments(dialogueCommentOutputAssembler.from(commentEntityMap, respondentId))
                .answersCount(commentEntityMap.size())
                .hasLikeByThisRespondent(hasLikeByThisRespondent)
                .likesCount(likesCount)
                .build();
    }

    @NonNull
    public Collection<DialogueQuestionOutput> from(
            Map<UUID, DialogueTopicQuestionEntity> entityMap,
            RespondentId respondentId
    ) {
        Collection<DialogueQuestionOutput> outputs = new ArrayList<>();

        entityMap.values().forEach(entity -> {
            outputs.add(from(entity, respondentId));
        });

        return outputs;
    }
}
