package fi.sangre.renesans.graphql.facade;

import com.google.api.client.util.Lists;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.model.parameter.ParameterItem;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.assemble.statistics.AfterGameCatalystStatisticsAssembler;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameParameterStatisticsOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.statistics.ParameterStatisticsService;
import fi.sangre.renesans.service.statistics.RespondentStatisticsService;
import fi.sangre.renesans.service.statistics.SurveyStatisticsService;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameFacade {
    private final OrganizationSurveyService organizationSurveyService;
    private final QuestionnaireAssembler questionnaireAssembler;
    private final AnswerService answerService;
    private final AnswerDao answerDao;
    private final SurveyStatisticsService surveyStatisticsService;
    private final RespondentStatisticsService respondentStatisticsService;
    private final ParameterStatisticsService parameterStatisticsService;
    private final AfterGameCatalystStatisticsAssembler afterGameCatalystStatisticsAssembler;
    private final ParameterUtils parameterUtils;
    private final SurveyUtils surveyUtils;
    private final MultilingualUtils multilingualUtils;
    @Qualifier(DAO_EXECUTOR_NAME)
    private final ThreadPoolTaskExecutor daoExecutor;

    @NonNull
    public Collection<AfterGameCatalystStatisticsOutput> afterGameOverviewCatalystsStatistics(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);

        final SurveyResult respondentStatistics = getRespondentStatistics(survey, principal);
        final SurveyResult allRespondentStatistics = surveyStatisticsService.calculateStatistics(survey);

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


        if (output.getOpenQuestion() != null && statistics != null) {
            Try.ofSupplier(() -> {
                if (ParameterId.GLOBAL_YOU_PARAMETER_ID.equals(parameterId)) { // return open answer for parameter if respondend is checking "you"
                    return answerDao.getAllOpenQuestionAnswers(surveyId, statistics.getRespondentIds());
                } else {
                    return answerDao.getPublicOpenQuestionAnswers(surveyId, statistics.getRespondentIds());
                }
            })
                    .onSuccess(answers -> output.getOpenQuestion().setAnswers(answers))
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
            surveyStatistics = surveyStatisticsService.calculateStatistics(survey);
        } else {
            surveyStatistics = parameterStatisticsService.calculateStatistics(survey, parameterId);
        }

        log.debug("Finished getting stats for Survey(id={}), Catalyst(id={}) Parameter(id={})", survey.getId(), catalyst.getId(), parameterId);

        return surveyStatistics;
    }

    private List<ParameterChild> getParameters(@NonNull final OrganizationSurvey survey, @NonNull final UserDetails principal) {
        final ImmutableList.Builder<ParameterChild> parameters = ImmutableList.builder();

        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;

            try {
                final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(respondent.getSurveyId(), respondent.getId());
                final QuestionnaireOutput questionnaire = questionnaireAssembler.from(respondent.getId(), survey, ImmutableMap.of(), ImmutableMap.of(), parameterAnswers.get());

                parameters.add(getGlobalYouParameter(survey));
                parameters.addAll(questionnaire.getParameters().stream()
                        .filter(QuestionnaireParameterOutput::isAnswered)
                        .map(parameter -> parameterUtils.findChildren(ParameterId.fromUUID(parameter.getSelectedAnswer()), surveyUtils.findParameter(parameter.getValue(), survey)))
                        .flatMap(Collection::stream)
                        .collect(toList()));

                parameters.add(getGlobalEveryoneParameter(survey));
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else if (principal instanceof UserPrincipal) {
            parameters.add(getGlobalEveryoneParameter(survey));
        }

        return parameters.build();
    }

    @NonNull
    public ParameterChild getGlobalYouParameter(@NonNull final OrganizationSurvey survey) {
        //TODO: get phrases from survey
        return ParameterItem.builder()
                .id(ParameterId.GLOBAL_YOU_PARAMETER_ID)
                .label(multilingualUtils.create(ImmutableMap.of("en", "you")))
                .build();
    }

    @NonNull
    public ParameterChild getGlobalEveryoneParameter(@NonNull final OrganizationSurvey survey) {
        //TODO: get phrases from survey
        return ParameterItem.builder()
                .id(ParameterId.GLOBAL_EVERYONE_PARAMETER_ID)
                .label(multilingualUtils.create(ImmutableMap.of("en", "Everyone")))
                .build();
    }

}
