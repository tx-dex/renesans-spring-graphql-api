package fi.sangre.renesans.service.statistics;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.dao.StatisticsDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Slf4j

@Service
public class RespondentStatisticsService {
    private final StatisticsDao statisticsDao;
    private final StatisticsService statisticsService;

    @NonNull
    // TODO: cache
    public SurveyStatistics calculateStatistics(@NonNull final OrganizationSurvey survey, @NonNull final RespondentId respondentId) {
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Map<QuestionId, QuestionStatistics> results = statisticsDao.getQuestionStatistics(surveyId, ImmutableSet.of(respondentId));
        return statisticsService.calculateStatistics(survey, results);
    }

    @NonNull
    public SurveyStatistics calculateStatistics(@NonNull final OrganizationSurvey survey, @NonNull final List<RespondentFilter> filters) {
        final SurveyId surveyId = new SurveyId(survey.getId());

        final Map<QuestionId, QuestionStatistics> results = statisticsDao.getQuestionStatistics(surveyId, filters);
        return statisticsService.calculateStatistics(survey, results);
    }
}
