package fi.sangre.renesans.graphql.facade.aftergame;

import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.dialogue.DialogueCommentOutputAssembler;
import fi.sangre.renesans.graphql.assemble.dialogue.DialogueTopicOutputAssembler;
import fi.sangre.renesans.graphql.assemble.dialogue.DialogueTopicQuestionOutputAssembler;
import fi.sangre.renesans.graphql.input.dialogue.DialogueCommentInput;
import fi.sangre.renesans.graphql.input.dialogue.DialogueTopicInput;
import fi.sangre.renesans.graphql.output.dialogue.*;
import fi.sangre.renesans.persistence.dialogue.model.*;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import fi.sangre.renesans.repository.dialogue.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j

// TODO: rename to DialogueService since it actively uses JPA directly?
@Component
public class DialogueFacade {
    private final AfterGameFacade afterGameFacade;

    private final DialogueTopicOutputAssembler dialogueTopicOutputAssembler;
    private final DialogueTopicQuestionOutputAssembler dialogueTopicQuestionOutputAssembler;
    private final DialogueCommentOutputAssembler dialogueCommentOutputAssembler;
    private final SurveyRepository surveyRepository;
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final DialogueTopicRepository dialogueTopicRepository;
    private final DialogueCommentLikeRepository dialogueCommentLikeRepository;
    private final DialogueQuestionLikeRepository dialogueQuestionLikeRepository;
    private final DialogueTopicQuestionRepository dialogueTopicQuestionRepository;
    private final DialogueTipRepository dialogueTipRepository;
    private final DialogueCommentRepository dialogueCommentRepository;
    private final EntityManagerFactory entityManagerFactory;

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

    public Collection<DialogueTopicOutput> getDialogueTopicsAdmin(
            @NonNull final UUID surveyId
    ) {
        List<DialogueTopicEntity> dialogueTopicEntities = dialogueTopicRepository.findAllBySurveyId(
                surveyId, Sort.by(Sort.Direction.ASC, "sortOrder")
        );

        return dialogueTopicOutputAssembler.from(dialogueTopicEntities);
    }

    public DialogueTopicOutput getDialogueTopicAdmin(
            @NonNull final UUID surveyTopicId
    ) {
        DialogueTopicEntity topicEntity = dialogueTopicRepository.getOne(surveyTopicId);

        return dialogueTopicOutputAssembler.from(topicEntity);
    }

    public DialogueCommentOutput postComment(
            @Nullable final UUID parentCommentId,
            @NonNull final UUID dialogueQuestionId,
            @NonNull final DialogueCommentInput input,
            @NonNull final RespondentId respondentId,
            @NonNull final SurveyId surveyId
            ) {
        DialogueCommentEntity parentComment = null;
        DialogueTopicQuestionEntity question = dialogueTopicQuestionRepository
                .findById(dialogueQuestionId)
                .orElseThrow(() -> new SurveyException("Could not find a question!"));

        if (parentCommentId != null) {
            parentComment = dialogueCommentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new SurveyException("Could not find a parent comment!"));
        }

        Survey survey = surveyRepository.findById(surveyId.getValue())
                .orElseThrow(() -> new SurveyException("Could not find a survey!"));

        SurveyRespondent surveyRespondent = surveyRespondentRepository.getOne(respondentId.getValue());

        DialogueCommentEntity commentEntity = DialogueCommentEntity.builder()
                .respondent(surveyRespondent)
                .parent(parentComment)
                .question(question)
                .survey(survey)
                .text(input.getText())
                .build();

        dialogueCommentRepository.saveAndFlush(commentEntity);

        DialogueCommentEntity updatedComment = dialogueCommentRepository.findById(commentEntity.getId())
                .orElseThrow(() -> new SurveyException("Could not get an updated comment entity from the database."));

        return dialogueCommentOutputAssembler.from(updatedComment, respondentId);
    }

    public DialogueCommentOutput likeOrUnlikeComment(@NonNull final UUID commentId,
                                                     @NonNull final SurveyId surveyId,
                                                     @NonNull final RespondentId respondentId) {
        DialogueCommentEntity commentEntity = dialogueCommentRepository.findById(commentId)
                .orElseThrow(() -> new SurveyException("Could not find a comment, no way to like/unlike it"));

        if (respondentId.getValue().equals(commentEntity.getRespondent().getId())) {
            throw new SurveyException("Liking/unliking your own comment is not allowed.");
        }

        Survey survey = surveyRepository.findById(surveyId.getValue())
                .orElseThrow(() -> new SurveyException("Could not find a survey!"));
        SurveyRespondent surveyRespondent = surveyRespondentRepository.getOne(respondentId.getValue());

        Collection<DialogueCommentLikeEntity> likes = commentEntity.getLikes();
        Optional<DialogueCommentLikeEntity> ownLike = likes.stream()
                .filter(like -> like.getRespondent().getId().equals(respondentId.getValue()))
                .findFirst();

        if (ownLike.isPresent()) {
            // if this user has already liked the comment, let's unlike it
            dialogueCommentLikeRepository.deleteById(ownLike.get().getId());
        } else {
            // like the comment if not yet
            dialogueCommentLikeRepository.saveAndFlush(
                    DialogueCommentLikeEntity.builder()
                            .comment(commentEntity)
                            .respondent(surveyRespondent)
                            .survey(survey)
                            .build()
            );
        }

        return dialogueCommentOutputAssembler.from(commentEntity, respondentId);
    }

    public DialogueQuestionOutput likeOrUnlikeQuestion(@NonNull final UUID questionId,
                                                     @NonNull final SurveyId surveyId,
                                                     @NonNull final RespondentId respondentId) {
        DialogueTopicQuestionEntity questionEntity = dialogueTopicQuestionRepository.findById(questionId)
                .orElseThrow(() -> new SurveyException("Could not find a question, no way to like/unlike it"));

        Survey survey = surveyRepository.findById(surveyId.getValue())
                .orElseThrow(() -> new SurveyException("Could not find a survey!"));
        SurveyRespondent surveyRespondent = surveyRespondentRepository.getOne(respondentId.getValue());

        Collection<DialogueQuestionLikeEntity> likes = questionEntity.getLikes();
        Optional<DialogueQuestionLikeEntity> ownLike = likes.stream()
                .filter(like -> like.getRespondent().getId().equals(respondentId.getValue()))
                .findFirst();

        if (ownLike.isPresent()) {
            // if this user has already liked the question, let's unlike it
            dialogueQuestionLikeRepository.deleteById(ownLike.get().getId());
        } else {
            // like the question if not yet
            dialogueQuestionLikeRepository.saveAndFlush(
                    DialogueQuestionLikeEntity.builder()
                            .question(questionEntity)
                            .respondent(surveyRespondent)
                            .survey(survey)
                            .build()
            );
        }

        return dialogueTopicQuestionOutputAssembler.from(questionEntity, respondentId);
    }

    public boolean deleteComment(@NonNull final UUID commentId,
                                               @NonNull final UserDetails principal) {
        DialogueCommentEntity commentEntity = dialogueCommentRepository.findById(commentId)
                .orElseThrow(() -> new SurveyException("Could not find a comment, is it already removed?"));

        // allow respondent to delete his own comment
        if (principal instanceof RespondentPrincipal) {
            RespondentPrincipal respondent = (RespondentPrincipal) principal;

            // only comment's author can remove it
            // removal should be cascade, i.e. all the replies will be removed automatically
            if (commentEntity.getRespondent().getId().equals(respondent.getId().getValue())) {
                dialogueCommentRepository.delete(commentEntity);
            } else {
                throw new SurveyException("A respondent cannot delete a comment that does not belong to him.");
            }

        // allow admin to delete any comment in a survey he's responsible for
        } else if (principal instanceof UserPrincipal) {
            dialogueCommentRepository.delete(commentEntity);
        } else {
            throw new SurveyException("Incorrect user type for deletion of a comment.");
        }

        return true;
    }

    public boolean changeSurveyDialogueActivation(@NonNull UUID surveyId, @NonNull Boolean isActive) {
        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException("The survey does not exist."));

        survey.setIsDialogueActive(isActive);
        surveyRepository.saveAndFlush(survey);

        return true;
    }

    public boolean deleteTopic(@NonNull UUID topicId) {
        DialogueTopicEntity dialogueTopicEntity = dialogueTopicRepository.findById(topicId)
                .orElseThrow(() -> new SurveyException("The topic does not exist."));

        dialogueTopicRepository.delete(dialogueTopicEntity);
        dialogueTopicRepository.flush();

        return true;
    }

    public boolean reorderTopic(@NonNull UUID surveyId, @NonNull UUID topicId, @NonNull Integer sortOrder) {
        DialogueTopicEntity dialogueTopicEntity = dialogueTopicRepository.findById(topicId)
                .orElseThrow(() -> new SurveyException("The topic does not exist."));

        if (!surveyId.equals(dialogueTopicEntity.getSurvey().getId())) {
            throw new SurveyException("A topic you're trying to re-order does not belong to this survey.");
        }

        dialogueTopicEntity.setSortOrder(sortOrder);
        dialogueTopicRepository.saveAndFlush(dialogueTopicEntity);

        return true;
    }

    public Collection<DialogueTopicOutput> storeTopics(
            @NonNull UUID surveyId,
            @NonNull Collection<DialogueTopicInput> inputs) {
        Collection<DialogueTopicOutput> topicOutputs = new ArrayList<>();

        EntityManager em = entityManagerFactory.createEntityManager();
        EntityTransaction tx = null;

        // remove all the topics that have disappeared
        List<DialogueTopicEntity> existingTopicEntities = dialogueTopicRepository.findAllBySurveyId(surveyId);
        List<DialogueTopicEntity> topicsToRemove = new ArrayList<>();
        List<DialogueTopicInput> inputsWithId = inputs.stream().filter(input -> input.getId() != null)
                .collect(Collectors.toCollection(ArrayList::new));

        for (DialogueTopicEntity topicEntity : existingTopicEntities) {
            boolean isItemRemoved = inputsWithId.stream().noneMatch(input -> input.getId().equals(topicEntity.getId()));
            if (isItemRemoved) topicsToRemove.add(topicEntity);
        }

        try {
            tx = em.getTransaction();
            tx.begin();

            dialogueTopicRepository.deleteAll(topicsToRemove);

            for (DialogueTopicInput input : inputs) {
                topicOutputs.add(storeTopic(input, surveyId));
            }

            tx.commit();
        } catch (Exception e) {
            try {
                log.error("Could not commit a dialogue topics transaction: " + e.getMessage());
                log.error(e.fillInStackTrace().getMessage());
                if (tx != null) tx.rollback();
            } catch (Exception te) {
                log.error("Failed to rollback a dialogue topics transaction: "  + te.getMessage());
                log.error(te.fillInStackTrace().getMessage());
            }
        } finally {
            em.close();
        }

        return topicOutputs;
    }

    public DialogueTopicOutput storeTopic(@NonNull DialogueTopicInput input, @NonNull UUID surveyId) {
        // check if the topic already exists (i.e. edit mode)
        if (input.getId() != null) {
            DialogueTopicEntity existingTopicEntity = dialogueTopicRepository.findById(input.getId())
                    .orElseThrow(() -> new SurveyException("Cannot edit a topic that does not exist."));

            return editTopic(existingTopicEntity, input);
        }

        Survey survey = surveyRepository.findById(surveyId)
                .orElseThrow(() -> new SurveyException("The survey linked to this topic does not exist."));

        return createTopic(input, survey);
    }

    private DialogueTopicOutput createTopic(@NonNull DialogueTopicInput input,
                                            @NonNull Survey survey) {
        Set<DialogueTipEntity> tipEntities = new HashSet<>();
        Set<DialogueTopicQuestionEntity> questionEntities = new HashSet<>();

        DialogueTopicEntity topicEntity = DialogueTopicEntity.builder()
                .active(input.isActive())
                .title(input.getTitle())
                .survey(survey)
                .sortOrder(input.getSortOrder())
                .build();

        input.getQuestions().forEach((questionInput) -> {
            DialogueTopicQuestionEntity questionEntity = DialogueTopicQuestionEntity.builder()
                    .title(questionInput.getTitle())
                    .topic(topicEntity)
                    .sortOrder(questionInput.getSortOrder())
                    .active(questionInput.isActive())
                    .build();

            questionEntities.add(questionEntity);
        });

        input.getTips().forEach((tipInput) -> {
            DialogueTipEntity tipEntity = DialogueTipEntity.builder()
                    .topic(topicEntity)
                    .text(tipInput.getText())
                    .build();

            tipEntities.add(tipEntity);
        });

        topicEntity.getQuestions().addAll(questionEntities);
        topicEntity.getTips().addAll(tipEntities);

        dialogueTopicRepository.saveAndFlush(topicEntity);

        DialogueTopicEntity createdTopic = dialogueTopicRepository.findById(topicEntity.getId()).orElseThrow(
                () -> new SurveyException("Could not get a newly created topic entity from the database.")
        );

        return dialogueTopicOutputAssembler.from(createdTopic);
    }

    // TODO: break the method into smaller ones?
    private DialogueTopicOutput editTopic(@NonNull DialogueTopicEntity existingTopicEntity,
                                          @NonNull DialogueTopicInput input) {
        Set<DialogueTipEntity> tipEntities = new HashSet<>();
        Set<DialogueTopicQuestionEntity> questionEntities = new HashSet<>();

        List<DialogueTopicQuestionEntity> previousQuestions = dialogueTopicQuestionRepository
                .findAllByTopic(existingTopicEntity);
        List<DialogueTipEntity> previousTips = dialogueTipRepository.findAllByTopic(existingTopicEntity);

        List<DialogueTopicQuestionEntity> questionsToRemove = new ArrayList<>();
        List<DialogueTipEntity> tipsToRemove = new ArrayList<>();

        existingTopicEntity.setTitle(input.getTitle());
        existingTopicEntity.setActive(input.isActive());
        existingTopicEntity.setSortOrder(input.getSortOrder());

        input.getQuestions().forEach((questionInput) -> {
            DialogueTopicQuestionEntity questionEntity = DialogueTopicQuestionEntity.builder()
                    .id(questionInput.getId())
                    .title(questionInput.getTitle())
                    .topic(existingTopicEntity)
                    .sortOrder(questionInput.getSortOrder())
                    .active(questionInput.isActive())
                    .build();

            if (questionInput.getId() != null) {
                DialogueTopicQuestionEntity previousQuestion = existingTopicEntity
                        .getQuestions().stream().filter((q) -> q.getId().equals(questionInput.getId())).findFirst()
                        .orElseThrow(() -> new SurveyException(
                                "Could not copy the children of question " +
                                        "because the question with such id does not exist")
                        );

                // avoid losing of comments and likes
                questionEntity.getComments().addAll(previousQuestion.getComments());
                questionEntity.getLikes().addAll(previousQuestion.getLikes());
            }

            questionEntities.add(questionEntity);
        });

        input.getTips().forEach((tipInput) -> {
            DialogueTipEntity tipEntity = DialogueTipEntity.builder()
                    .id(tipInput.getId())
                    .topic(existingTopicEntity)
                    .text(tipInput.getText())
                    .build();

            tipEntities.add(tipEntity);
        });

        existingTopicEntity.getTips().clear();
        existingTopicEntity.getQuestions().clear();
        existingTopicEntity.getTips().addAll(tipEntities);
        existingTopicEntity.getQuestions().addAll(questionEntities);

        previousQuestions.forEach((previousQuestion -> {
            boolean isQuestionStillExists = questionEntities.stream().anyMatch(
                    updatedQuestion -> updatedQuestion.getId() != null
                            && updatedQuestion.getId().equals(previousQuestion.getId())
            );

            if (!isQuestionStillExists) questionsToRemove.add(previousQuestion);
        }));

        previousTips.forEach((previousTip -> {
            boolean isTipStillExists = tipEntities.stream().anyMatch(
                    updatedTip -> updatedTip.getId() != null && updatedTip.getId().equals(previousTip.getId())
            );

            if (!isTipStillExists) tipsToRemove.add(previousTip);
        }));

        dialogueTopicRepository.save(existingTopicEntity);
        if (questionsToRemove.size() > 0) dialogueTopicQuestionRepository.deleteAll(questionsToRemove);
        if (tipsToRemove.size() > 0) dialogueTipRepository.deleteAll(tipsToRemove);

        DialogueTopicEntity updatedTopic = dialogueTopicRepository.findById(existingTopicEntity.getId()).orElseThrow(
                () -> new SurveyException("Could not get an updated topic entity from the database.")
        );

        return dialogueTopicOutputAssembler.from(updatedTopic);
    }
}
