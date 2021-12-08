package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.graphql.facade.aftergame.AfterGameFacade;
import fi.sangre.renesans.graphql.facade.QuestionnaireFacade;
import fi.sangre.renesans.graphql.facade.aftergame.DialogueFacade;
import fi.sangre.renesans.graphql.input.answer.CatalystOpenQuestionAnswerInput;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionAnswerInput;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionRateInput;
import fi.sangre.renesans.graphql.input.answer.ParameterAnswerInput;
import fi.sangre.renesans.graphql.input.dialogue.DialogueCommentInput;
import fi.sangre.renesans.graphql.input.discussion.DiscussionCommentInput;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.dialogue.DialogueTopicOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameCommentOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AppMutations implements GraphQLMutationResolver {
    private final QuestionnaireFacade questionnaireFacade;
    private final AfterGameFacade afterGameFacade;
    private final DialogueFacade dialogueFacade;
    private final ResolverHelper resolverHelper;

    // NOTICE!!!
    // this is public and respondent does not have token for that yet. Do not authorize it!!!
    @NonNull
    public AuthorizationOutput openQuestionnaire(@NonNull final UUID id, @NonNull final String invitationHash) {
        return questionnaireFacade.openQuestionnaire(id, invitationHash);
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public QuestionnaireOutput goToQuestions(@NonNull final UUID id,
                                             @Nullable final String languageCode,
                                             @NonNull final DataFetchingEnvironment environment) {

        log.debug("Opening questions page: questionnaire(id={})", id);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.goToQuestions(id , resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT')")
    public QuestionnaireOutput answerOrSkipLikertQuestion(@NonNull final LikertQuestionAnswerInput answer,
                                                          @Nullable final String languageCode,
                                                          @NonNull final DataFetchingEnvironment environment) {
        log.debug("Answering question: {}", answer);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.answerLikertQuestion(answer, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT')")
    public QuestionnaireOutput rateLikertQuestion(@NonNull final LikertQuestionRateInput rate,
                                                  @Nullable final String languageCode,
                                                  @NonNull final DataFetchingEnvironment environment) {
        log.debug("Rating question: {}", rate);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.rateLikertQuestion(rate, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT')")
    public QuestionnaireOutput answerOrSkipCatalystQuestion(@NonNull final CatalystOpenQuestionAnswerInput answer,
                                                            @Nullable final String languageCode,
                                                            @NonNull final DataFetchingEnvironment environment) {
        log.debug("Answering question: {}", answer);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.answerCatalystQuestion(answer, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT') or hasRole('GUEST')")
    public QuestionnaireOutput answerParameter(@NonNull final ParameterAnswerInput answer,
                                               @Nullable final String languageCode,
                                               @NonNull final DataFetchingEnvironment environment) {
        log.debug("Answering parameter: {}", answer);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.answerParameter(answer, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasRole('RESPONDENT') or hasRole('GUEST')")
    public QuestionnaireOutput consentQuestionnairePolicy(@NonNull final UUID id,
                                               @NonNull final Boolean consent,
                                               @Nullable final String languageCode,
                                               @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.consentQuestionnaire(id, consent, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public AfterGameDiscussionOutput commentOnAfterGameDiscussion(@NonNull final UUID questionnaireId,
                                                                  @NonNull final UUID discussionId,
                                                                  @NonNull final DiscussionCommentInput input,
                                                                  @Nullable final String languageCode,
                                                                  @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.commentOnDiscussion(questionnaireId,
                discussionId,
                input,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public AfterGameCommentOutput likeOnAfterGameComment(@NonNull final UUID questionnaireId,
                                                         @NonNull final UUID discussionId,
                                                         @NonNull final UUID commentId,
                                                         @NonNull final Boolean like,
                                                         @Nullable final String languageCode,
                                                         @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.likeOnComment(questionnaireId,
                new QuestionId(discussionId),
                commentId,
                like,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public DialogueTopicOutput postDialogueComment(@NonNull final UUID questionnaireId,
                                                   @Nullable final UUID parentCommentId,
                                                   @NonNull final UUID dialogueQuestionId,
                                                   @NonNull final DialogueCommentInput input,
                                                   @Nullable final String languageCode,
                                                   @NonNull final DataFetchingEnvironment environment
    ) {


        return new DialogueTopicOutput();
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public DialogueTopicOutput likeDialogueComment(@NonNull final UUID questionnaireId,
                                                   @NonNull final UUID commentId,
                                                   @NonNull final Boolean like,
                                                   @Nullable final String languageCode,
                                                   @NonNull final DataFetchingEnvironment environment
   ) {
        return new DialogueTopicOutput();
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public DialogueTopicOutput deleteDialogueComment(@NonNull final UUID questionnaireId,
                                                        @NonNull final UUID commentId,
                                                        @Nullable final String languageCode,
                                                        @NonNull final DataFetchingEnvironment environment
    ) {
        return new DialogueTopicOutput();
    }
}