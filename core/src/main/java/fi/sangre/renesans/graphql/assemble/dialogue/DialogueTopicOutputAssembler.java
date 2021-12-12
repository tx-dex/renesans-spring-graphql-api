package fi.sangre.renesans.graphql.assemble.dialogue;

import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.graphql.output.dialogue.DialogueTopicOutput;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicEntity;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicQuestionEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class DialogueTopicOutputAssembler {
    @Autowired
    private DialogueTopicQuestionOutputAssembler dialogueTopicQuestionOutputAssembler;

    @Autowired
    private DialogueTipOutputAssembler dialogueTipOutputAssembler;

    @NonNull
    public DialogueTopicOutput from(DialogueTopicEntity entity, RespondentId respondentId) {
        List<DialogueTopicQuestionEntity> questionEntityList = entity.getQuestions();

        return DialogueTopicOutput.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .questions(dialogueTopicQuestionOutputAssembler.from(questionEntityList, respondentId))
                .active(entity.getActive())
                .questionsCount(questionEntityList.size())
                .sortOrder(entity.getSortOrder())
                .tips(dialogueTipOutputAssembler.from(entity.getTips()))
                .build();
    }

    @NonNull
    public DialogueTopicOutput from(DialogueTopicEntity entity) {
        List<DialogueTopicQuestionEntity> questionEntityList = entity.getQuestions();

        return DialogueTopicOutput.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .questions(dialogueTopicQuestionOutputAssembler.from(questionEntityList))
                .active(entity.getActive())
                .questionsCount(questionEntityList.size())
                .sortOrder(entity.getSortOrder())
                .tips(dialogueTipOutputAssembler.from(entity.getTips()))
                .build();
    }

    @NonNull
    public Collection<DialogueTopicOutput> from(Collection<DialogueTopicEntity> entities, RespondentId respondentId) {
        Collection<DialogueTopicOutput> outputs = new ArrayList<>();

        entities.forEach(entity -> {
            outputs.add(from(entity, respondentId));
        });

        return outputs;
    }

    @NonNull
    public Collection<DialogueTopicOutput> from(Collection<DialogueTopicEntity> entities) {
        Collection<DialogueTopicOutput> outputs = new ArrayList<>();

        entities.forEach(entity -> {
            outputs.add(from(entity));
        });

        return outputs;
    }
}
