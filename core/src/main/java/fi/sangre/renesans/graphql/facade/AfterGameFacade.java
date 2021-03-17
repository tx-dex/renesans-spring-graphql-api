package fi.sangre.renesans.graphql.facade;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.aaa.GuestPrincipal;
import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.InvitationAssembler;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.DiscussionDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.model.parameter.ParameterItem;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.discussion.AfterGameDiscussionAssembler;
import fi.sangre.renesans.graphql.assemble.questionnaire.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.assemble.statistics.AfterGameCatalystStatisticsAssembler;
import fi.sangre.renesans.graphql.input.MailInvitationInput;
import fi.sangre.renesans.graphql.input.discussion.DiscussionCommentInput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameCommentOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameParameterStatisticsOutput;
import fi.sangre.renesans.persistence.discussion.model.ActorEntity;
import fi.sangre.renesans.persistence.discussion.model.CommentEntity;
import fi.sangre.renesans.service.AfterGameService;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.TranslationService;
import fi.sangre.renesans.service.statistics.ParameterStatisticsService;
import fi.sangre.renesans.service.statistics.RespondentStatisticsService;
import fi.sangre.renesans.service.statistics.SurveyStatisticsService;
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
    private static final String PARAMETER_GROUP = "after_game_default_groups";
    private static final String YOU_PARAMETER_KEY = "you";
    private static final String EVERYONE_PARAMETER_KEY = "everyone";

    private final OrganizationSurveyService organizationSurveyService;
    private final QuestionnaireAssembler questionnaireAssembler;
    private final AnswerService answerService;
    private final AnswerDao answerDao;
    private final DiscussionDao discussionDao;
    private final SurveyStatisticsService surveyStatisticsService;
    private final RespondentStatisticsService respondentStatisticsService;
    private final ParameterStatisticsService parameterStatisticsService;
    private final AfterGameCatalystStatisticsAssembler afterGameCatalystStatisticsAssembler;
    private final AfterGameDiscussionAssembler afterGameDiscussionAssembler;
    private final AfterGameService afterGameService;
    private final InvitationAssembler invitationAssembler;
    private final ParameterUtils parameterUtils;
    private final SurveyUtils surveyUtils;
    private final MultilingualUtils multilingualUtils;
    private final TranslationService translationService;
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
        if (surveyUtils.isAfterGameEnabled(survey)) {
            allRespondentStatistics = surveyStatisticsService.calculateStatistics(survey);
        } else {
            allRespondentStatistics = null;
        }

        return afterGameCatalystStatisticsAssembler.from(survey, respondentStatistics, allRespondentStatistics);
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
            statistics = getParameterStatistics(survey, catalyst, parameterId, principal);
        }

        final AfterGameCatalystStatisticsOutput output = afterGameCatalystStatisticsAssembler.from(catalyst, null, statistics);

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
                            question.setAnswers(answers.getOrDefault(question.getId(), ImmutableList.of()));
                        });
                    })
                    .onFailure(ex -> log.warn("Cannot get answers", ex))
                    .getOrElseThrow(ex -> new SurveyException("Cannot get answers"));
        }

        return output;
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

        parameters.forEach(parameter -> {
            log.debug("Get async stats for Survey(id={}), Catalyst(id={}) Parameter(id={})", survey.getId(), catalyst.getId(), parameter.getId());
            futures.add(io.vavr.concurrent.Future.of(daoExecutor, () -> {
                final SurveyResult statistics = getParameterStatistics(survey, catalyst, parameter.getId(), principal);
                final AfterGameCatalystStatisticsOutput output = afterGameCatalystStatisticsAssembler.from(catalyst, null, statistics);
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
                    .filter(v -> Objects.nonNull(v.getResult())) // skipping empty results from the list
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            throw new SurveyException("Cannot get statistics");
        }
    }

    private <T> boolean isAllSuccess(@NonNull final Collection<Try<T>> tries) {
        return tries.stream()
                .map(Try::isSuccess)
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
    }

    @NonNull
    private OrganizationSurvey getSurvey(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
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
                                                @NonNull final Catalyst catalyst,
                                                @NonNull final ParameterId parameterId,
                                                @NonNull final UserDetails principal) {
        log.debug("Start getting stats for Survey(id={}), Catalyst(id={}) Parameter(id={})", survey.getId(), catalyst.getId(), parameterId);
        final SurveyResult surveyStatistics;

        if (ParameterId.GLOBAL_YOU_PARAMETER_ID.equals(parameterId)) {
            surveyStatistics = getRespondentStatistics(survey, principal);
        } else if (ParameterId.GLOBAL_EVERYONE_PARAMETER_ID.equals(parameterId)) {
            if (surveyUtils.isAfterGameEnabled(survey)) {
                surveyStatistics = surveyStatisticsService.calculateStatistics(survey);
            } else {
                surveyStatistics = null;
            }
        } else {
            if (surveyUtils.isAfterGameEnabled(survey)) {
                surveyStatistics = parameterStatisticsService.calculateStatistics(survey, parameterId);
            } else {
                surveyStatistics = null;
            }
        }

        log.debug("Finished getting stats for Survey(id={}), Catalyst(id={}) Parameter(id={})", survey.getId(), catalyst.getId(), parameterId);

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

        if (questionnaire != null) {
                parameters.addAll(questionnaire.getParameters().stream()
                        .filter(QuestionnaireParameterOutput::isAnswered)
                        .map(parameter -> parameterUtils.findChildren(ParameterId.fromUUID(parameter.getSelectedAnswer()), surveyUtils.findParameter(parameter.getValue(), survey)))
                        .flatMap(Collection::stream)
                        .collect(toList()));

        }

        parameters.add(getGlobalEveryoneParameter(survey));

        return parameters.build();
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

        final DiscussionQuestion question = survey.getDiscussionQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new SurveyException("Cannot get discussion"));

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

}
