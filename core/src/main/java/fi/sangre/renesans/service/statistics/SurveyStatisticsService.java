package fi.sangre.renesans.service.statistics;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.StatisticsDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.config.properties.StatisticsProperties;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j

@Service
public class SurveyStatisticsService {
    private final StatisticsProperties properties;
    private final StatisticsDao statisticsDao;
    private final AnswerDao answerDao;
    private final StatisticsService statisticsService;

    @NonNull
    // TODO: cache
    public SurveyResult calculateStatistics(@NonNull final OrganizationSurvey survey) {
        final SurveyId surveyId = new SurveyId(survey.getId());

        final Set<RespondentId> respondentIds = answerDao.getAnsweredRespondentIds(surveyId);

        if (respondentIds.size() < properties.getMinRespondentCount()) {
            return SurveyResult.builder()
                    .respondentIds(ImmutableSet.of())
                    .statistics(statisticsService.calculateStatistics(survey, ImmutableMap.of()))
                    .build();
        } else {
            final Map<QuestionId, QuestionStatistics> results = statisticsDao.getQuestionStatistics(surveyId);
            return SurveyResult.builder()
                    .respondentIds(respondentIds)
                    .statistics(statisticsService.calculateStatistics(survey, results))
                    .build();
        }
    }
}
