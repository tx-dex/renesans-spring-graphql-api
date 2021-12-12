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

import java.util.*;

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
        Collection<DialogueCommentEntity> commentEntities = entity.getComments();

        boolean hasLikeByThisRespondent = dialogueQuestionLikeRepository
                .countDialogueQuestionLikeEntitiesByQuestionEqualsAndRespondentIdEquals(
                        entity, respondentId.getValue()
                ) > 0;

        int likesCount = dialogueQuestionLikeRepository.countDialogueQuestionLikeEntitiesByQuestionEquals(entity);

        return DialogueQuestionOutput.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .active(entity.getActive())
                .sortOrder(entity.getSortOrder())
                .comments(dialogueCommentOutputAssembler.from(commentEntities, respondentId))
                .answersCount(commentEntities.size())
                .hasLikeByThisRespondent(hasLikeByThisRespondent)
                .likesCount(likesCount)
                .build();
    }

    @NonNull
    public Collection<DialogueQuestionOutput> from(
            Set<DialogueTopicQuestionEntity> entityList,
            RespondentId respondentId
    ) {
        Collection<DialogueQuestionOutput> outputs = new ArrayList<>();

        entityList.forEach(entity -> {
            outputs.add(from(entity, respondentId));
        });

        return outputs;
    }

    @NonNull
    public Collection<DialogueQuestionOutput> from(Set<DialogueTopicQuestionEntity> entityList) {
        Collection<DialogueQuestionOutput> outputs = new ArrayList<>();

        entityList.forEach(entity -> outputs.add(DialogueQuestionOutput.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .sortOrder(entity.getSortOrder())
                .active(entity.getActive())
                .image("")
                .build()));

        return outputs;
    }
}
