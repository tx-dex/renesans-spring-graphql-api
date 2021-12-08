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
import fi.sangre.renesans.graphql.output.dialogue.*;
import fi.sangre.renesans.persistence.dialogue.model.*;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import fi.sangre.renesans.repository.dialogue.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    private final DialogueTopicQuestionOutputAssembler dialogueTopicQuestionOutputAssembler;

    @Autowired
    private final DialogueCommentOutputAssembler dialogueCommentOutputAssembler;

    @Autowired
    private final SurveyRepository surveyRepository;

    private final SurveyRespondentRepository surveyRespondentRepository;

    @Autowired
    private final DialogueTopicRepository dialogueTopicRepository;

    @Autowired
    private final DialogueCommentLikeRepository dialogueCommentLikeRepository;

    @Autowired
    private final DialogueQuestionLikeRepository dialogueQuestionLikeRepository;

    @Autowired
    private final DialogueTopicQuestionRepository dialogueTopicQuestionRepository;

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
        // TODO: uncomment
        /*
        afterGameFacade.validateQuestionnairePermissions(questionnaireId, principal);
        DialogueTopicEntity dialogueTopicEntity = dialogueTopicRepository.findById(topicId).orElse(null);

        if (dialogueTopicEntity == null) {
            log.warn("Could not find a dialogue topic with id(id={})", topicId);
            throw new SurveyException("Could not find a dialogue topic by id");
        } */

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
                .answersCount(3)
                .likesCount(4)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question2 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("Another question title")
                .active(true)
                .sortOrder(2)
                .likesCount(2)
                .answersCount(2)
                .comments(commentsList1)
                .build();

        DialogueQuestionOutput question3 = DialogueQuestionOutput.builder()
                .id(UUID.randomUUID())
                .title("Closed question title")
                .active(false)
                .sortOrder(2)
                .likesCount(3)
                .answersCount(1)
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
                .id(UUID.randomUUID())
                .respondent(surveyRespondent)
                .parent(parentComment)
                .question(question)
                .survey(survey)
                .text(input.getText())
                .build();

        dialogueCommentRepository.saveAndFlush(commentEntity);

        return dialogueCommentOutputAssembler.from(commentEntity, respondentId);
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

        Collection<DialogueCommentLikeEntity> likes = commentEntity.getLikes().values();
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

        Collection<DialogueQuestionLikeEntity> likes = questionEntity.getLikes().values();
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
}
