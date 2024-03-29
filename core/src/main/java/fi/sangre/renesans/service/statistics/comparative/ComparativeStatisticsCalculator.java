package fi.sangre.renesans.service.statistics.comparative;

import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;

import java.util.Map;

public interface ComparativeStatisticsCalculator {
    StatisticsResult getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics);
    String getLabel(String languageCode);
}
