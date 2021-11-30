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

        return dialogueTopicOutputAssembler.from(dialogueTopicEntities, new RespondentId(questionnaireId));
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

        return dialogueTopicOutputAssembler.from(dialogueTopicEntity, new RespondentId(questionnaireId));
    }
}
