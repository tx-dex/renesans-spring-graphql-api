package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.graphql.facade.aftergame.AfterGameFacade;
import fi.sangre.renesans.graphql.facade.QuestionnaireFacade;
import fi.sangre.renesans.graphql.facade.aftergame.DialogueFacade;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.dialogue.DialogueQuestionOutput;
import fi.sangre.renesans.graphql.output.dialogue.DialogueTopicOutput;
import fi.sangre.renesans.graphql.output.dialogue.DialogueTotalStatisticsOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import fi.sangre.renesans.graphql.output.parameter.SurveyParameterOutput;
import fi.sangre.renesans.graphql.output.statistics.*;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AppQueries implements GraphQLQueryResolver {
    private final QuestionnaireFacade questionnaireFacade;
    private final AfterGameFacade afterGameFacade;
    private final DialogueFacade dialogueFacade;
    private final ResolverHelper resolverHelper;

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'READ')")
    public QuestionnaireOutput questionnaire(@NonNull final UUID id, @Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting questionnaire(id={})", id);
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireFacade.getQuestionnaire(id,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameCatalystStatisticsOutput> afterGameOverviewCatalystsStatistics(@NonNull final UUID questionnaireId, @Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game overview statistics questionnaire(id={})", questionnaireId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameOverviewCatalystsStatistics(questionnaireId,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public AfterGameCatalystStatisticsOutput afterGameDetailedCatalystStatistics(@NonNull final UUID questionnaireId,
                                                                                 @NonNull final UUID catalystId,
                                                                                 @Nullable final UUID parameterValue,
                                                                                 @Nullable final String languageCode,
                                                                                 @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game detailed statistics questionnaire(id={}, catalystId={})", questionnaireId, catalystId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameDetailedCatalystStatistics(questionnaireId,
                catalystId,
                parameterValue,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameDetailedDriverStatisticsOutput> afterGameDetailedDriversStatistics(@NonNull final UUID questionnaireId,
                                                                                                  @Nullable final UUID parameterValue,
                                                                                                  @Nullable final String languageCode,
                                                                                                  @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game detailed drivers questionnaire(id={})", questionnaireId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameDetailedDriversStatistics(
                questionnaireId,
                parameterValue,
                resolverHelper.getRequiredPrincipal(environment)
        );
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameQuestionStatisticsOutput> afterGameDetailedQuestionsStatistics(@NonNull final UUID questionnaireId,
                                                                                              @Nullable final UUID parameterValue,
                                                                                              @Nullable final String languageCode,
                                                                                              @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game detailed questions statistics for questionnaire(id={})", questionnaireId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameDetailedQuestionsStatistics(questionnaireId,
                parameterValue,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameOpenQuestionStatisticsOutput> afterGameDetailedOpenQuestionsStatistics(@NonNull final UUID questionnaireId,
                                                                                              @Nullable final UUID parameterValue,
                                                                                              @Nullable final String languageCode,
                                                                                              @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game open questions statistics for questionnaire(id={})", questionnaireId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameOpenQuestionStatistics(questionnaireId,
                parameterValue,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameParameterStatisticsOutput> afterGameRespondentParametersStatistics(@NonNull final UUID questionnaireId,
                                                                                                  @NonNull final UUID catalystId,
                                                                                                  @Nullable final String languageCode,
                                                                                                  @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after game parameter statistics questionnaire(id={}, catalystId={})", questionnaireId, catalystId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameRespondentParametersStatistics(questionnaireId,
                catalystId,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameDiscussionOutput> afterGameDiscussions(@NonNull final UUID questionnaireId,
                                                                      @NonNull final Boolean active,
                                                                      @Nullable final String languageCode,
                                                                      @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after discussions questionnaire(id={})", questionnaireId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameDiscussions(questionnaireId,
                active,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public AfterGameDiscussionOutput afterGameDiscussion(@NonNull final UUID questionnaireId,
                                                         @NonNull final UUID discussionId,
                                                         @Nullable final String languageCode,
                                                         @NonNull final DataFetchingEnvironment environment) {
        log.debug("Getting after discussion questionnaire(id={}, discussionId={})", questionnaireId, discussionId);
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.afterGameDiscussion(questionnaireId,
                discussionId,
                resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public AfterGameOverviewParticipantsOutput afterGameOverviewParticipants(
            @NonNull final UUID questionnaireId,
            @NonNull final DataFetchingEnvironment environment
    ) {
        log.debug("Getting after game participants overview for questionnaire(id={})", questionnaireId);

        return afterGameFacade.afterGameParticipantsOverview(
                questionnaireId,
                resolverHelper.getRequiredPrincipal(environment)
        );
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<AfterGameDiscussionOutput> afterGameOverviewLatestDiscussions(
            @NonNull final UUID questionnaireId,
            @Nullable final String languageCode,
            @NonNull final DataFetchingEnvironment environment
    ) {
        log.debug("Getting two latest after game discussions for questionnaire(id={})", questionnaireId);

        resolverHelper.setLanguageCode(languageCode, environment);
        return afterGameFacade.afterGameLatestActiveDiscussions(questionnaireId, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public AfterGameOverviewVisionAttainmentIndicatorOutput afterGameOverviewVisionAttainmentIndicator(
            @NonNull final UUID questionnaireId,
            @NonNull final DataFetchingEnvironment environment
    ) {
        return afterGameFacade.afterGameOverviewVisionAttainmentIndicator(
                questionnaireId,
                resolverHelper.getRequiredPrincipal(environment)
        );
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<SurveyParameterOutput> afterGameParameters(
            @NonNull final UUID questionnaireId,
            @Nullable final String languageCode,
            @NonNull final DataFetchingEnvironment environment
    ) {
        log.debug("Getting parameters list for questionnaire(id={})", questionnaireId);

        resolverHelper.setLanguageCode(languageCode, environment);
        return afterGameFacade.afterGameParameters(questionnaireId, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public DialogueTotalStatisticsOutput afterGameDialogueTotalStatistics(
            @NonNull final UUID questionnaireId,
            @Nullable final String languageCode,
            @NonNull final DataFetchingEnvironment environment
    ) {
        log.debug("Getting dialogue total stats for questionnaire(id={})", questionnaireId);

        resolverHelper.setLanguageCode(languageCode, environment);
        return dialogueFacade.getDialogueTotalStatistics(questionnaireId, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public Collection<DialogueTopicOutput> afterGameDialogueTopics(
            @NonNull final UUID questionnaireId,
            @Nullable final String languageCode,
            @NonNull final DataFetchingEnvironment environment
    ) {
        log.debug("Getting dialogue topics for questionnaire(id={})", questionnaireId);

        resolverHelper.setLanguageCode(languageCode, environment);
        return dialogueFacade.getDialogueTopics(questionnaireId, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionnaireId, 'survey', 'READ')")
    public DialogueTopicOutput afterGameDialogueTopic(
            @NonNull final UUID questionnaireId,
            @NonNull final UUID topicId,
            @Nullable final String languageCode,
            @NonNull final DataFetchingEnvironment environment
    ) {
        log.debug("Getting dialogue topic by id: " + topicId);

        resolverHelper.setLanguageCode(languageCode, environment);
        return dialogueFacade.getDialogueTopic(
                questionnaireId,
                topicId,
                resolverHelper.getRequiredPrincipal(environment)
        );
    }

    @NonNull
    @PreAuthorize("hasPermission(#questionId, 'survey', 'READ')")
    public AnswerDistributionsOutput getAnswerDistribution(
            @NonNull final UUID questionId
    ) {
        return afterGameFacade.answerDistributionsOutput(questionId);
    }
}
