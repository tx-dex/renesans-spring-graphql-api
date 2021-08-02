package fi.sangre.renesans.service.statistics;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.StatisticsDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.statistics.CatalystStatistics;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.config.properties.StatisticsProperties;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j

@Service
public class RespondentStatisticsService {
    private final StatisticsProperties properties;
    private final StatisticsDao statisticsDao;
    private final AnswerDao answerDao;
    private final StatisticsService statisticsService;

    @NonNull
    // TODO: cache
    public SurveyResult calculateStatistics(@NonNull final OrganizationSurvey survey, @NonNull final RespondentId respondentId) {
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Map<QuestionId, QuestionStatistics> results = statisticsDao.getQuestionStatistics(surveyId, ImmutableSet.of(respondentId));
        return SurveyResult.builder()
                .respondentIds(ImmutableSet.of(respondentId))
                .statistics(statisticsService.calculateStatistics(survey, results))
                .build();
    }

    @NonNull
    public SurveyResult calculateStatistics(@NonNull final OrganizationSurvey survey, @NonNull final List<RespondentFilter> filters) {
        final SurveyId surveyId = new SurveyId(survey.getId());

        final Set<RespondentId> respondentIds = answerDao.getAnsweredRespondentIds(surveyId, filters);
        if (respondentIds.size() < properties.getMinRespondentCount()) {
            return SurveyResult.EMPTY;
        } else {
            final Map<QuestionId, QuestionStatistics> results = statisticsDao.getQuestionStatistics(surveyId, respondentIds);
            return SurveyResult.builder()
                    .respondentIds(respondentIds)
                    .statistics(statisticsService.calculateStatistics(survey, results))
                    .build();
        }
    }


    @NonNull
    public Double calculateVisionAttainmentIndicator(@NonNull final OrganizationSurvey survey, @NonNull final RespondentId respondentId) {
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Map<QuestionId, QuestionStatistics> questionStatistics = statisticsDao.getQuestionStatistics(surveyId, ImmutableSet.of(respondentId));
        final List<DriverStatistics> driverStatistics = statisticsService.calculateDriversStatistics(survey, questionStatistics);
        final List<CatalystStatistics> catalystsStatistics = statisticsService.calculateCatalystsStatistics(survey, driverStatistics, questionStatistics);

        return statisticsService.calculateVisionAttainmentIndicator(catalystsStatistics);
    }
}
