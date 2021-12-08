package fi.sangre.renesans.graphql.facade.aftergame;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.dialogue.DialogueTopicOutputAssembler;
import fi.sangre.renesans.graphql.output.dialogue.*;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTopicEntity;
import fi.sangre.renesans.repository.dialogue.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class DialogueFacade {
    private final AfterGameFacade afterGameFacade;

    @Autowired
    private final DialogueTopicOutputAssembler dialogueTopicOutputAssembler;

    @Autowired
    private final DialogueTopicRepository dialogueTopicRepository;

    @Autowired
    private final DialogueCommentLikeRepository dialogueCommentLikeRepository;

    @Autowired
    private final DialogueQuestionLikeRepository dialogueQuestionLikeRepository;

    @Autowired
    private final DialogueCommentRepository dialogueCommentRepository;

    @NonNull
    public DialogueTotalStatisticsOutput getDialogueTotalStatistics(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = afterGameFacade.getSurvey(questionnaireId, principal);
        UUID surveyId = survey.getId();

        int likesTotal = dialogueCommentLikeRepository.countAllBySurveyId(surveyId)
                + dialogueQuestionLikeRepository.countAllBySurveyId(surveyId);
        int answersCount = dialogueCommentRepository.countAllBySurveyId(surveyId);
        int activeTopicsCount = dialogueTopicRepository.countAllBySurveyIdAndActiveTrue(surveyId);

        return DialogueTotalStatisticsOutput.builder()
                .id(UUID.randomUUID())
                .answersCount(answersCount)
                .likesCount(likesTotal)
                .activeTopicsCount(activeTopicsCount)
                .build();
    }

    public Collection<DialogueTopicOutput> getDialogueTopics(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = afterGameFacade.getSurvey(questionnaireId, principal);

        List<DialogueTopicEntity> dialogueTopicEntities = dialogueTopicRepository.findAllBySurveyId(
                survey.getId(), Sort.by(Sort.Direction.ASC, "sortOrder")
        );

        // TODO: uncomment the line and remove mock data
        // return dialogueTopicOutputAssembler.from(dialogueTopicEntities, new RespondentId(questionnaireId));
        return getFakeTopicsList();
    }

    public DialogueTopicOutput getDialogueTopic(
            @NonNull final UUID questionnaireId,
            @NonNull final UUID topicId,
            @NonNull final UserDetails principal
    ) {
        afterGameFacade.validateQuestionnairePermissions(questionnaireId, principal);
        DialogueTopicEntity dialogueTopicEntity = dialogueTopicRepository.findById(topicId).orElse(null);

        if (dialogueTopicEntity == null) {
            log.warn("Could not find a dialogue topic with id(id={})", topicId);
            throw new SurveyException("Could not find a dialogue topic by id");
        }

        // TODO: uncomment the line and remove mock data
        // return dialogueTopicOutputAssembler.from(dialogueTopicEntity, new RespondentId(questionnaireId));
        return getFakeTopic();
    }

    private List<DialogueTopicOutput> getFakeTopicsList() {
        List<DialogueTopicOutput> topics = new ArrayList<>();
        topics.add(getFakeTopic());
        topics.add(getFakeTopic());
        topics.add(getFakeTopic());
        return topics;
    }

    private DialogueTopicOutput getFakeTopic() {
        DialogueCommentOutput comment1Reply = DialogueCommentOutput
                .builder()
                .id(UUID.randomUUID())
                .text("Ei, see on vana")
                .likesCount(0)
                .hasLikeByThisRespondent(false)
                .createdAt("2021-11-20T11:03:51.612Z")
                .respondentColor("#00ff00")
                .authorRespondentId(UUID.randomUUID())
                .isOwnedByThisRespondent(false)
                .build();

        DialogueCommentOutput comment1 = DialogueCommentOutput
                .builder()
                .id(UUID.randomUUID())
                .text("Kas me seda küsimust arutame?")
                .likesCount(2)
                .replies(Collections.singletonList(comment1Reply))
                .hasLikeByThisRespondent(true)
                .createdAt("2021-11-20T10:00:00.612Z")
                .respondentColor("#ee0000")
                .authorRespondentId(UUID.randomUUID())
                .isOwnedByThisRespondent(true)
                .build();


        DialogueCommentOutput comment2 = DialogueCommentOutput
                .builder()
                .id(UUID.randomUUID())
                .text("O hi!")
                .likesCount(1)
                .replies(Collections.singletonList(comment1Reply))
                .hasLikeByThisRespondent(true)
                .createdAt("2021-11-20T10:30:00.612Z")
                .respondentColor("#ee0000")
                .authorRespondentId(UUID.randomUUID())
                .isOwnedByThisRespondent(false)
                .build();


        List<DialogueCommentOutput> commentsList1 = Arrays.asList(comment1, comment2);
        DialogueQuestionOutput question1 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("First question title")
                .active(true)
                .sortOrder(1)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question2 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("Another question title")
                .active(true)
                .sortOrder(2)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question3 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("Closed question title")
                .active(false)
                .sortOrder(2)
                // don't send any comments since the question is archived
                .comments(Collections.emptyList())
                .build();

        DialogueTipOutput tip1 = DialogueTipOutput.builder().id(UUID.randomUUID()).text("Some tip will be here").build();
        DialogueTipOutput tip2 = DialogueTipOutput.builder().id(UUID.randomUUID()).text("Lorem ipsum sit amet").build();

        return DialogueTopicOutput.builder()
                .id(UUID.randomUUID())
                .title("Topic #1")
                .active(true)
                .questionsCount(3)
                .tips(Arrays.asList(tip1, tip2))
                .questions(Arrays.asList(question1, question2, question3))
                .sortOrder(1)
                .build();
    }
}
