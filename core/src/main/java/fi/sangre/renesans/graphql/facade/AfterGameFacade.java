package fi.sangre.renesans.graphql.facade;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.aaa.RespondentPrincipal;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.QuestionnaireAssembler;
import fi.sangre.renesans.graphql.assemble.statistics.AfterGameCatalystStatisticsAssembler;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import fi.sangre.renesans.graphql.output.statistics.*;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
    private final StatisticsService statisticsService;
    private final AfterGameCatalystStatisticsAssembler afterGameCatalystStatisticsAssembler;
    private final ParameterUtils parameterUtils;
    private final SurveyUtils surveyUtils;

    @NonNull
    public Collection<AfterGameCatalystStatisticsOutput> afterGameOverviewCatalystsStatistics(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final SurveyId surveyId = new SurveyId(survey.getId());

        final SurveyStatistics respondentStatistics;
        final SurveyStatistics allRespondentStatistics;
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            final Map<QuestionId, QuestionStatistics> results = answerDao.getQuestionStatistics(surveyId, ImmutableSet.of(respondent.getId()));
            respondentStatistics = statisticsService.calculateStatistics(survey, results);
        } else {
            respondentStatistics = statisticsService.calculateStatistics(survey, ImmutableMap.of());
        }

        final Map<QuestionId, QuestionStatistics> results = answerDao.getQuestionStatistics(surveyId);
        allRespondentStatistics = statisticsService.calculateStatistics(survey, results);

        return afterGameCatalystStatisticsAssembler.from(survey, respondentStatistics, allRespondentStatistics);
    }

    @NonNull
    public AfterGameCatalystStatisticsOutput afterGameDetailedCatalystStatistics(@NonNull final UUID questionnaireId,
                                                                                 @NonNull final UUID catalystId,
                                                                                 @Nullable final UUID parameterValue,
                                                                                 @NonNull final UserDetails principal) {
        final QuestionnaireOutput questionnaire = getQuestionnaire(questionnaireId, principal);

        return questionnaire.getCatalysts().stream()
                .filter(catalyst -> catalyst.getId().equals(catalystId))
                .map(catalyst -> AfterGameCatalystStatisticsOutput.builder()
                        .id(catalyst.getId())
                        .titles(catalyst.getTitles().getPhrases())
                        .respondentResult(0d)
                        .respondentGroupResult(0d)
                        .drivers(catalyst.getDrivers().stream()
                                .map(driver -> AfterGameDriverStatisticsOutput.builder()
                                        .titles(driver.getTitles().getPhrases())
                                        .respondentResult(0d)
                                        .respondentGroupResult(0d)
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .questions(catalyst.getQuestions().stream()
                                .map(question -> AfterGameQuestionStatisticsOutput.builder()
                                        .titles(question.getTitles())
                                        .rate(0d)
                                        .result(0d)
                                        .build())
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                        .openQuestion(Optional.ofNullable(catalyst.getCatalystQuestion())
                                .map(question -> AfterGameOpenQuestionOutput.builder()
                                        .titles(question.getTitles())
                                        .answers(ImmutableList.of())
                                        .build())
                                .orElse(null))
                        .build())
                .findFirst()
                .orElseThrow(() -> new SurveyException("Catalyst not found"));
    }

    @NonNull
    public Collection<AfterGameParameterStatisticsOutput> afterGameRespondentParametersStatistics(@NonNull final UUID questionnaireId,
                                                                                                  @NonNull final UUID catalystId,
                                                                                                  @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);
        final QuestionnaireOutput questionnaire;
        final ImmutableList.Builder<AfterGameParameterStatisticsOutput> result = ImmutableList.builder();
        if (principal instanceof RespondentPrincipal) {
            final RespondentPrincipal respondent = (RespondentPrincipal) principal;
            try {
                final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(respondent.getSurveyId(), respondent.getId());

                questionnaire = questionnaireAssembler.from(respondent.getId(), survey, ImmutableMap.of(), ImmutableMap.of(), parameterAnswers.get());

                result.add(AfterGameParameterStatisticsOutput.builder()
                        .value(ParameterId.GLOBAL_YOU_PARAMETER_ID.asString())
                        .titles(ImmutableMap.of("en", "you"))
                        .result(0d)
                        .build());

                result.addAll(questionnaire.getParameters().stream()
                        .filter(QuestionnaireParameterOutput::isAnswered)
                        .map(parameter -> parameterUtils.findChildren(ParameterId.fromUUID(parameter.getSelectedAnswer()), surveyUtils.findParameter(parameter.getValue(), survey)))
                        .flatMap(Collection::stream)
                        .map(parameter -> AfterGameParameterStatisticsOutput.builder()
                        .titles(parameter.getLabel().getPhrases())
                                .value(parameter.getId().asString())
                                .result(0d)
                        .build())
                .collect(toList()));
            } catch (final InterruptedException | ExecutionException ex) {
                log.warn("Cannot get questionnaire for respondent(id={})", respondent.getId());
                throw new InternalServiceException("Cannot get questionnaire");
            }
        } else if (principal instanceof UserPrincipal) {
            questionnaire = questionnaireAssembler.from(survey);
        }

        return result.build();
    }

    @NonNull
    private QuestionnaireOutput getQuestionnaire(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
        final OrganizationSurvey survey = getSurvey(questionnaireId, principal);

        return questionnaireAssembler.from(survey);
    }

    @NonNull
    public OrganizationSurvey getSurvey(@NonNull final UUID questionnaireId, @NonNull final UserDetails principal) {
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
}
