package fi.sangre.renesans.graphql.facade.aftergame;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.aaa.GuestPrincipal;
import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.InvitationAssembler;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.DiscussionDao;
import fi.sangre.renesans.application.dao.StatisticsDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.filter.RespondentParameterFilter;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.model.parameter.ParameterItem;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.OpenQuestionAnswerAssembler;
import fi.sangre.renesans.graphql.assemble.SurveyParameterOutputAssembler;
import fi.sangre.renesans.graphql.assemble.discussion.AfterGameDiscussionAssembler;
import fi.sangre.renesans.graphql.assemble.questionnaire.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.assemble.statistics.*;
import fi.sangre.renesans.graphql.input.MailInvitationInput;
import fi.sangre.renesans.graphql.input.discussion.DiscussionCommentInput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameCommentOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import fi.sangre.renesans.graphql.output.statistics.*;
import fi.sangre.renesans.graphql.output.parameter.SurveyParameterOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOverviewParticipantsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOverviewVisionAttainmentIndicatorOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameParameterStatisticsOutput;
import fi.sangre.renesans.persistence.discussion.model.ActorEntity;
import fi.sangre.renesans.persistence.discussion.model.CommentEntity;
import fi.sangre.renesans.persistence.model.RespondentStateCounters;
import fi.sangre.renesans.persistence.model.statistics.AnswerDistribution;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;
import fi.sangre.renesans.service.*;
import fi.sangre.renesans.service.statistics.ParameterStatisticsService;
import fi.sangre.renesans.service.statistics.RespondentStatisticsService;
import fi.sangre.renesans.service.statistics.SurveyStatisticsService;
import fi.sangre.renesans.service.statistics.comparative.ComparativeStatisticsCalculator;
import fi.sangre.renesans.service.statistics.comparative.ComparativeStatisticsCalculatorFactory;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameFacade {
    private static final String PARAMETER_GROUP = "finish";
    private static final String YOU_PARAMETER_KEY = "you";
    private static final String EVERYONE_PARAMETER_KEY = "everyone";
    private static final Integer LATEST_DISCUSSIONS_LIMIT = 2;

    private final OrganizationSurveyService organizationSurveyService;
    private final QuestionnaireAssembler questionnaireAssembler;
    private final AnswerService answerService;
    private final AnswerDao answerDao;
    private final DiscussionDao discussionDao;
    private final SurveyDao surveyDao;
    private final StatisticsDao statisticsDao;
    private final SurveyStatisticsService surveyStatisticsService;
    private final RespondentStatisticsService respondentStatisticsService;
    private final ParameterStatisticsService parameterStatisticsService;
    private final AfterGameCatalystStatisticsAssembler afterGameCatalystStatisticsAssembler;
    private final AfterGameQuestionsStatisticsAssembler afterGameQuestionsStatisticsAssembler;
    private final AfterGameOpenQuestionsStatisticsAssembler afterGameOpenQuestionsStatisticsAssembler;
    private final AfterGameDetailedDriversStatisticsAssembler afterGameDetailedDriversStatisticsAssembler;
    private final AfterGameDiscussionAssembler afterGameDiscussionAssembler;
    private final SurveyParameterOutputAssembler surveyParameterOutputAssembler;
    private final OpenQuestionAnswerAssembler openQuestionAnswerAssembler;
    private final AfterGameService afterGameService;
    private final InvitationAssembler invitationAssembler;
    private final ParameterUtils parameterUtils;
    private final SurveyUtils surveyUtils;
    private final MultilingualUtils multilingualUtils;
    private final TranslationService translationService;
    private final StatisticsService statisticsService;
    private final AnswerDistributionAssembler answerDistributionAssembler;
    private final AfterGameComparativeStatisticsAssembler afterGameComparativeStatisticsAssembler;

    @Qualifier(DAO_EXECUTOR_NAME)
    private final ThreadPoolTaskExecutor daoExecutor;

    @NonNull
    public OrganizationSurvey inviteToAfterGame(@NonNull final SurveyId surveyId,
                                                @NonNull final MailInvitationInput input,
                                                @NonNull final UserDetails principal) {
        if (principal instanceof UserPrincipal) {
            final UserPrincipal user = (UserPrincipal) principal;
            final Invitation invitation = invitationAssembler.from(input);
            final Pair<String, String> replyTo = Pair.of(user.getName(), user.getEmail());
            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(surveyId);
                afterGameService.inviteToAfterGame(surveyId, invitation, replyTo);

                return survey;
            } catch (final Exception ex) {
                log.warn("Cannot invite to after game {}", surveyId, ex);
                throw new InternalServiceException("Cannot invite to after game");
            }
        } else {
            throw new SurveyException("Only admins can invite");
        }
    }

    @NonNull
    public OrganizationSurvey inviteToAfterGameDiscussion(@NonNull final SurveyId surveyId,
                                                          @NonNull final QuestionId questionId,
                                                          @NonNull final MailInvitationInput input,
                                                          @NonNull final UserDetails principal) {
        if (principal instanceof UserPrincipal) {
            final UserPrincipal user = (UserPrincipal) principal;
            final Invitation invitation = invitationAssembler.from(input);
            final Pair<String, String> replyTo = Pair.of(user.getName(), user.getEmail());

            try {
                final OrganizationSurvey survey = organizationSurveyService.getSurvey(surveyId);
                afterGameService.inviteToAfterGameDiscussion(surveyId, questionId, invitation, replyTo);

                return survey;
            } catch (final Exception ex) {
                log.warn("Cannot invite to after game {}", surveyId, ex);
                throw new InternalServiceException("Cannot invite to after game");
            }
        } else {
            throw new SurveyException("Only admins can invite");
        }
    }

    @NonNull
    public Collection<AfterGameCatalystStatisticsOutput> afterGameOverviewCatalystsStatistics(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);

        final SurveyResult respondentStatistics = getRespondentStatistics(survey, principal);
        final SurveyResult allRespondentStatistics;

        allRespondentStatistics = surveyStatisticsService.calculateStatistics(survey);

        Long respondentsAnswered = surveyDao.countRespondentsBySurvey(new SurveyId(survey.getId())).getAnswered();

        return afterGameCatalystStatisticsAssembler.from(survey, respondentStatistics, allRespondentStatistics, respondentsAnswered);
    }

    @NonNull
    public AfterGameCatalystStatisticsOutput afterGameDetailedCatalystStatistics(@NonNull final UUID questionnaireId,
                                                                                 @NonNull final UUID catalystId,
                                                                                 @Nullable final UUID parameterValue, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Catalyst catalyst = Optional.ofNullable(surveyUtils.findCatalyst(catalystId, survey))
                .orElseThrow(() -> new SurveyException("Catalyst not found in the survey"));

        final SurveyResult statistics;
        final ParameterId parameterId;
        if (parameterValue == null) {
            parameterId = null;
            statistics = null;
        } else {
            parameterId = new ParameterId(parameterValue);
            statistics = getParameterStatistics(survey, parameterId, principal);
        }

        RespondentStateCounters respondentStateCounters = surveyDao.countRespondentsBySurvey(new SurveyId(survey.getId()));
        Long respondentsAnswered = respondentStateCounters != null ? respondentStateCounters.getAnswered() : 0L;

        final AfterGameCatalystStatisticsOutput output = afterGameCatalystStatisticsAssembler.from(
                catalyst, null, statistics, respondentsAnswered
        );

        if (output.getOpenQuestions() != null && statistics != null) {
            Try.ofSupplier(() -> {
                if (ParameterId.GLOBAL_YOU_PARAMETER_ID.equals(parameterId)) { // return open answer for parameter if respondent is checking "you"
                    return answerDao.getAllOpenAnswers(surveyId,
                            new CatalystId(catalystId),
                            statistics.getRespondentIds());
                } else {
                    return answerDao.getPublicOpenAnswers(surveyId,
                            new CatalystId(catalystId),
                            statistics.getRespondentIds());
                }
            })
                    .onSuccess(answers -> {
                        output.getOpenQuestions().forEach(question -> {
                            question.setAnswers(openQuestionAnswerAssembler.from(answers.getOrDefault(question.getId(), ImmutableList.of())));
                        });
                    })
                    .onFailure(ex -> log.warn("Cannot get answers", ex))
                    .getOrElseThrow(ex -> new SurveyException("Cannot get answers"));
        }

        return output;
    }

    @NonNull
    public Collection<AfterGameDetailedDriverStatisticsOutput> afterGameDetailedDriversStatistics(
            @NonNull final UUID questionnaireId,
            @Nullable final UUID parameterValue,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final boolean isAfterGameEnabled = SurveyState.AFTER_GAME.equals(survey.getState());
        final Set<RespondentId> respondentIds;

        if (isAfterGameEnabled) {
            respondentIds = getRespondentIdsFromStatistics(
                    getSurveyResultForAfterGame(survey, principal, parameterValue)
            );
        } else {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            respondentIds = new HashSet<>(Collections.singletonList(respondent.getId()));
        }

        final SurveyId surveyId = new SurveyId(survey.getId());
        final Map<QuestionId, QuestionStatistics> questionStatisticsMap = statisticsDao.getQuestionStatistics(surveyId, respondentIds);

        return afterGameDetailedDriversStatisticsAssembler.from(
                statisticsService.calculateDetailedDriversStatistics(survey, questionStatisticsMap),
                survey
        );
    }

    @NonNull
    public Collection<AfterGameQuestionStatisticsOutput> afterGameDetailedQuestionsStatistics(
            @NonNull final UUID questionnaireId,
            @Nullable final UUID parameterValue,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final boolean isAfterGameEnabled = SurveyState.AFTER_GAME.equals(survey.getState());
        final Set<RespondentId> respondentIds;

        if (isAfterGameEnabled) {
            respondentIds = getRespondentIdsFromStatistics(
                    getSurveyResultForAfterGame(survey, principal, parameterValue)
            );
        } else {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            respondentIds = new HashSet<>(Collections.singletonList(respondent.getId()));
        }


        final SurveyId surveyId = new SurveyId(survey.getId());

        return afterGameQuestionsStatisticsAssembler.from(
                statisticsDao.getQuestionStatistics(surveyId, respondentIds),
                survey
        );
    }
    @NonNull
    public Collection<AfterGameOpenQuestionStatisticsOutput> afterGameOpenQuestionStatistics(
            @NonNull final UUID questionnaireId,
            @Nullable final UUID parameterValue,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final boolean isAfterGameEnabled = SurveyState.AFTER_GAME.equals(survey.getState());
        final Set<RespondentId> respondentIds;
        final RespondentId loggedRespondent;

        if(principal instanceof RespondentPrincipal) {
            loggedRespondent = ((RespondentPrincipal) principal).getId();
        } else {
            loggedRespondent = null;
        }

        if (isAfterGameEnabled) {
            respondentIds = getRespondentIdsFromStatistics(getSurveyResultForAfterGame(survey, principal, parameterValue));
        } else {
            respondentIds = Collections.singleton(loggedRespondent);
        }

        final SurveyId surveyId = new SurveyId(survey.getId());

        return afterGameOpenQuestionsStatisticsAssembler.from(
                statisticsDao.getOpenQuestionStatistics(surveyId, respondentIds, loggedRespondent),
                survey
        );
    }

    @NonNull
    public Collection<AfterGameParameterStatisticsOutput> afterGameRespondentParametersStatistics(@NonNull final UUID questionnaireId,
                                                                                                  @NonNull final UUID catalystId,
                                                                                                  @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final Catalyst catalyst = Optional.ofNullable(surveyUtils.findCatalyst(catalystId, survey))
                .orElseThrow(() -> new SurveyException("Catalyst not found in the survey"));

        final List<io.vavr.concurrent.Future<AfterGameParameterStatisticsOutput>> futures = Lists.newArrayList();
        final List<ParameterChild> parameters = getParameters(survey, principal);

        RespondentStateCounters respondentStateCounters = surveyDao.countRespondentsBySurvey(new SurveyId(survey.getId()));
        Long respondentsAnswered = respondentStateCounters.getAnswered();

        parameters.forEach(parameter -> {
            log.debug("Get async stats for Survey(id={}), Catalyst(id={}) Parameter(id={})", survey.getId(), catalyst.getId(), parameter.getId());
            futures.add(io.vavr.concurrent.Future.of(daoExecutor, () -> {
                final SurveyResult statistics = getParameterStatistics(survey, parameter.getId(), principal);
                final AfterGameCatalystStatisticsOutput output = afterGameCatalystStatisticsAssembler.from(
                        catalyst, null, statistics, respondentsAnswered
                );
                return AfterGameParameterStatisticsOutput.builder()
                        .titles(parameter.getLabel().getPhrases())
                        .value(parameter.getId().asString())
                        .result(output.getRespondentGroupResult())
                        .build();
            }));
        });

        final List<Try<AfterGameParameterStatisticsOutput>> tries = futures.stream()
                .map(e -> Try.ofSupplier(e::get)
                        .onFailure(ex -> log.warn("Cannot get stats", ex))
                ).collect(toList());

        if (isAllSuccess(tries)) {
            return tries.stream()
                    .map(Try::get)
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            throw new SurveyException("Cannot get statistics");
        }
    }

    public AfterGameComparativeStatisticsOutput afterGameComparativeParameterStatistics(@NonNull final UUID questionnaireId,
                                                                                        @NonNull final String topicId,
                                                                                        @NonNull final TopicType topicType,
                                                                                        @NonNull final UserDetails principal,
                                                                                        @NonNull final String languageCode) throws Exception {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        SurveyId surveyId = new SurveyId(survey.getId());

        ComparativeStatisticsCalculator comparativeStatisticsCalculator = ComparativeStatisticsCalculatorFactory.getCalculator(
                topicType,
                topicId,
                survey,
                surveyUtils,
                statisticsService);

        List<Parameter> parameters = parameterUtils.getAllChildren(survey.getParameters());
        Map<ParameterId, StatisticsResult> parameterStatistics = new HashMap<>();

        parameters.forEach(parameter -> {
            StatisticsResult statisticsResult = getParameterStatisticsResult(parameter, surveyId, comparativeStatisticsCalculator);
            parameterStatistics.put(parameter.getId(), statisticsResult);
        });

        StatisticsResult totalStatisticsResult = getStatisticsResult(surveyId, answerDao.getAnsweredRespondentIds(surveyId), comparativeStatisticsCalculator);

        return afterGameComparativeStatisticsAssembler.from(
                comparativeStatisticsCalculator.getLabel(languageCode),
                topicType,
                totalStatisticsResult,
                parameters,
                parameterStatistics,
                languageCode);
    }

    private StatisticsResult getParameterStatisticsResult(Parameter parameter, SurveyId surveyId, ComparativeStatisticsCalculator comparativeStatisticsCalculator) {
        List<UUID> parameterIds = parameterUtils.getAllChildren(parameter).stream().map(childParameter -> parameter.getId().getValue()).collect(toList());
        parameterIds.add(parameter.getId().getValue());

        Set<RespondentId> respondentIds = answerDao.getAnsweredRespondentIds(surveyId,
                ImmutableList.of(RespondentParameterFilter.builder()
                    .values(parameterIds)
                    .build()));

        return getStatisticsResult(surveyId, respondentIds, comparativeStatisticsCalculator);
    }

    private StatisticsResult getStatisticsResult(SurveyId surveyId, Set<RespondentId> respondentIds, ComparativeStatisticsCalculator comparativeStatisticsCalculator) {
        Map<QuestionId, QuestionStatistics> questionStatistics = statisticsDao.getQuestionStatistics(surveyId, respondentIds);
        return comparativeStatisticsCalculator.getStatistics(questionStatistics);
    }

    private <T> boolean isAllSuccess(@NonNull final Collection<Try<T>> tries) {
        return tries.stream()
                .map(Try::isSuccess)
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
    }

    @NonNull
    public OrganizationSurvey getSurvey(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey;
        if (principal instanceof RespondentPrincipal) {
            survey = organizationSurveyService.getSurvey(((RespondentPrincipal) principal).getSurveyId());
        } else if (principal instanceof GuestPrincipal) {
            survey = organizationSurveyService.getSurvey(((GuestPrincipal) principal).getSurveyId());
        } else if (principal instanceof UserPrincipal) {
            survey = organizationSurveyService.getSurvey(questionnaireId);
        } else {
            throw new SurveyException("Invalid principal");
        }

        return survey;
    }

    public void validateQuestionnairePermissions(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        getSurvey(questionnaireId, principal);
    }

    @Nullable
    private SurveyResult getRespondentStatistics(@NonNull final OrganizationSurvey survey, @NonNull final UserDetails principal) {
        final SurveyResult respondentStatistics;
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            respondentStatistics = respondentStatisticsService.calculateStatistics(survey, respondent.getId());
        } else {
            respondentStatistics = null;
        }

        return respondentStatistics;
    }

    @Nullable
    private SurveyResult getParameterStatistics(@NonNull final OrganizationSurvey survey,
                                                @NonNull final ParameterId parameterId,
                                                @NonNull final UserDetails principal) {
        log.debug("Start getting stats for Survey(id={}), Parameter(id={})", survey.getId(), parameterId);
        final SurveyResult surveyStatistics;

        if (ParameterId.GLOBAL_YOU_PARAMETER_ID.equals(parameterId)) {
            surveyStatistics = getRespondentStatistics(survey, principal);
        } else if (ParameterId.GLOBAL_EVERYONE_PARAMETER_ID.equals(parameterId)) {
            surveyStatistics = surveyStatisticsService.calculateStatistics(survey);
        } else {
            surveyStatistics = parameterStatisticsService.calculateStatistics(survey, parameterId);
        }

        log.debug("Finished getting stats for Survey(id={}), Parameter(id={})", survey.getId(), parameterId);

        return surveyStatistics;
    }

    @NonNull
    private List<ParameterChild> getParameters(@NonNull final OrganizationSurvey survey, @NonNull final UserDetails principal) {
        final ImmutableList.Builder<ParameterChild> parameters = ImmutableList.builder();

        final QuestionnaireOutput questionnaire;
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            parameters.add(getGlobalYouParameter(survey));

            try {
                final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(respondent.getSurveyId(), respondent.getId());
                questionnaire = questionnaireAssembler.from(respondent.getId(), survey, ImmutableMap.of(), ImmutableMap.of(), parameterAnswers.get());
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else if (principal instanceof GuestPrincipal) {
            final GuestPrincipal guest = (GuestPrincipal) principal;

            try {
                final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(guest.getSurveyId(), guest.getId());
                questionnaire = questionnaireAssembler.from(guest.getId(), survey, parameterAnswers.get());
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for guest(id={})", guest.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else {
            questionnaire = null;
        }

        if (survey.getState() == SurveyState.AFTER_GAME) {
            if (questionnaire != null) {
                parameters.addAll(questionnaire.getParameters().stream()
                        .filter(QuestionnaireParameterOutput::isAnswered)
                        .map(parameter -> parameterUtils.findChildren(ParameterId.fromUUID(parameter.getSelectedAnswer()), surveyUtils.findParameter(parameter.getValue(), survey)))
                        .flatMap(Collection::stream)
                        .collect(toList()));

            }

            parameters.add(getGlobalEveryoneParameter(survey));
        }

        return parameters.build();
    }

    @NonNull
    public AnswerDistributionsOutput answerDistributionsOutput(
            @NonNull final UUID questionnaireId,
            @NonNull final UUID questionId,
            @Nullable final UUID parameterValue,
            @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        List<AnswerDistribution> responseDistributions = statisticsDao.getResponseDistributions(surveyId, questionId, parameterValue, principal);
        List<AnswerDistribution> rateDistributions = statisticsDao.getRateDistributions(surveyId, questionId, parameterValue, principal);

        return answerDistributionAssembler.from(responseDistributions, rateDistributions);
    }

    @NonNull
    public Collection<AfterGameDiscussionOutput> afterGameDiscussions(@NonNull final UUID questionnaireId,
                                                                      @NonNull final Boolean active,
                                                                      @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        final Long actorId = getActorId(principal);
        final List<DiscussionQuestion> questions = survey.getDiscussionQuestions().stream()
                .filter(question -> active.equals(question.isActive()))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        final Set<QuestionId> questionIds = questions.stream()
                .map(DiscussionQuestion::getId)
                .collect(Collectors.toSet());


        final Map<QuestionId, List<CommentEntity>> discussions = discussionDao.findDiscussions(surveyId, questionIds);

        return afterGameDiscussionAssembler.from(questions, discussions, actorId);
    }

    @NonNull
    public Collection<AfterGameDiscussionOutput> afterGameLatestActiveDiscussions(@NonNull final UUID questionnaireId,
                                                                            @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        final Long actorId = getActorId(principal);
        final List<DiscussionQuestion> questions = survey.getDiscussionQuestions().stream()
                .filter(DiscussionQuestion::isActive)
                .sorted(Comparator.nullsLast(Comparator.comparing(DiscussionQuestion::getCreatedDate).reversed()))
                .limit(LATEST_DISCUSSIONS_LIMIT)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        final Set<QuestionId> questionIds = questions.stream()
                .map(DiscussionQuestion::getId)
                .collect(Collectors.toSet());

        final Map<QuestionId, List<CommentEntity>> discussions = discussionDao.findDiscussions(surveyId, questionIds);

        return afterGameDiscussionAssembler.from(questions, discussions, actorId);
    }

    @NonNull
    public AfterGameOverviewParticipantsOutput afterGameParticipantsOverview(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        final RespondentStateCounters counters = surveyDao.countRespondentsBySurvey(surveyId);

        return afterGameDiscussionAssembler.from(counters);
    }

    @NonNull
    public AfterGameDiscussionOutput afterGameDiscussion(@NonNull final UUID questionnaireId,
                                                         @NonNull final UUID discussionId,
                                                         @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        final Long actorId = getActorId(principal);
        final DiscussionQuestion question = survey.getDiscussionQuestions().stream()
                .filter(q -> discussionId.equals(q.getId().getValue()))
                .findFirst()
                .orElseThrow(() -> new SurveyException("Cannot get discussion"));
        final List<CommentEntity> discussion = discussionDao.findDiscussion(surveyId, question.getId());

        return afterGameDiscussionAssembler.from(question, discussion, actorId);
    }

    @NonNull
    public Collection<SurveyParameterOutput> afterGameParameters(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);

        return surveyParameterOutputAssembler.from(survey.getParameters());
    }

    @NonNull
    public AfterGameDiscussionOutput commentOnDiscussion(@NonNull final UUID questionnaireId,
                                                         @NonNull final UUID discussionId,
                                                         @NonNull final DiscussionCommentInput input,
                                                         @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());
        final String comment = Optional.ofNullable(input.getText())
                .map(StringUtils::trimToNull)
                .orElseThrow(() -> new SurveyException("Input.text cannot be null or empty"));

        final ActorEntity actor = createActor(principal);
        final DiscussionQuestion question = survey.getDiscussionQuestions().stream()
                .filter(q -> discussionId.equals(q.getId().getValue()))
                .findFirst()
                .orElseThrow(() -> new SurveyException("Cannot get discussion"));

        discussionDao.createOrUpdateComment(surveyId, question.getId(), actor, input.getCommentId(), comment);

        final List<CommentEntity> discussion = discussionDao.findDiscussion(surveyId, question.getId());

        //TODO: implement
        return afterGameDiscussionAssembler.from(question, discussion, actor.getId());
    }

    @NonNull
    public AfterGameCommentOutput likeOnComment(@NonNull final UUID questionnaireId,
                                                @NonNull final QuestionId questionId,
                                                @NonNull final UUID commentId,
                                                @NonNull final Boolean liked,
                                                @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        final ActorEntity actor = createActor(principal);

        return afterGameDiscussionAssembler.from(discussionDao.likeComment(
                surveyId, commentId, actor, liked),
                actor.getId());
    }

    @NonNull
    public ActorEntity createActor(@NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            return discussionDao.createOrGetActor(respondent.getSurveyId(), respondent.getId());
        } else if (principal instanceof GuestPrincipal) {
            final GuestPrincipal guest = (GuestPrincipal) principal;
            return discussionDao.createOrGetActor(guest.getSurveyId(), guest.getId());
        } else {
            throw new SurveyException("Admin can not comment on discussion");
        }
    }

    @Nullable
    private Long getActorId(@NonNull final UserDetails principal) {
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            return Optional.ofNullable(discussionDao.findActor(respondent.getSurveyId(), respondent.getId()))
                    .map(ActorEntity::getId)
                    .orElse(null);
        } else {
            return null;
        }
    }

    @NonNull
    private ParameterChild getGlobalYouParameter(@NonNull final OrganizationSurvey survey) {
        return ParameterItem.builder()
                .id(ParameterId.GLOBAL_YOU_PARAMETER_ID)
                .label(getParametersText(survey, YOU_PARAMETER_KEY))
                .build();
    }

    @NonNull
    private ParameterChild getGlobalEveryoneParameter(@NonNull final OrganizationSurvey survey) {
        return ParameterItem.builder()
                .id(ParameterId.GLOBAL_EVERYONE_PARAMETER_ID)
                .label(getParametersText(survey, EVERYONE_PARAMETER_KEY))
                .build();
    }

    public AfterGameOverviewVisionAttainmentIndicatorOutput afterGameOverviewVisionAttainmentIndicator(
            @NonNull final UUID questionnaireId,
            @NonNull final UserDetails principal
    ) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);

        return AfterGameOverviewVisionAttainmentIndicatorOutput.builder()
                .value(respondentStatisticsService.calculateVisionAttainmentIndicator(survey))
                .build();
    }

    @NonNull
    private MultilingualText getParametersText(@NonNull final OrganizationSurvey survey, @NonNull final String parameter) {
        final MultilingualText defaults = translationService.getPhrases(PARAMETER_GROUP, parameter);

        final MultilingualText surveySpecific = Optional.ofNullable(survey)
                .map(OrganizationSurvey::getStaticTexts)
                .map(v -> v.get(PARAMETER_GROUP))
                .map(StaticTextGroup::getTexts)
                .map(v -> v.get(parameter))
                .orElseGet(multilingualUtils::empty);

        return multilingualUtils.combine(defaults, surveySpecific);
    }

    @Nullable
    private SurveyResult getSurveyResultForAfterGame(
            @NonNull final OrganizationSurvey survey,
            @NonNull final UserDetails principal,
            @Nullable final UUID parameterValue
    ) {
        final ParameterId parameterId;

        if (parameterValue == null) {
            parameterId = new ParameterId(ParameterId.GLOBAL_EVERYONE_PARAMETER_ID);
        } else {
            parameterId = new ParameterId(parameterValue);
        }

        return getParameterStatistics(survey, parameterId, principal);
    }

    @NonNull
    private Set<RespondentId> getRespondentIdsFromStatistics(SurveyResult statistics) {
        return statistics == null ? ImmutableSet.of() : ImmutableSet.copyOf(statistics.getRespondentIds());
    }
}
