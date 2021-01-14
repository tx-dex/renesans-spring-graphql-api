package fi.sangre.renesans.service.statistics;

import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j

@Service
public class ParameterStatisticsService {
    private final AnswerDao answerDao;
    private final StatisticsService statisticsService;

    @NonNull
    // TODO: cache
    public SurveyStatistics calculateStatistics(@NonNull final OrganizationSurvey survey, @NonNull final ParameterId parameterId) {
        final SurveyId surveyId = new SurveyId(survey.getId());
        //TODO: implement;
//        final Map<QuestionId, QuestionStatistics> results = answerDao.getQuestionStatistics(surveyId, ImmutableSet.of(respondentId));
//        return statisticsService.calculateStatistics(survey, results);
        return null;
    }
}
