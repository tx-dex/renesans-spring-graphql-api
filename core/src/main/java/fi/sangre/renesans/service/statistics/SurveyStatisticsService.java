package fi.sangre.renesans.service.statistics;

import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j

@Service
public class SurveyStatisticsService {
    private final AnswerDao answerDao;
    private final StatisticsService statisticsService;

    @NonNull
    // TODO: cache
    public SurveyStatistics calculateStatistics(@NonNull final OrganizationSurvey survey) {
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Map<QuestionId, QuestionStatistics> results = answerDao.getQuestionStatistics(surveyId);
        return statisticsService.calculateStatistics(survey, results);
    }
}
