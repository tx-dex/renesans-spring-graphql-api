package fi.sangre.renesans.service.statistics.comparative;

import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;

import java.util.Map;

public class OverallComparativeStatisticCalculator implements ComparativeStatisticsCalculator {
    @Override
    public StatisticsResult getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics) {
        return null;
    }

    @Override
    public String getLabel(String languageCode) {
        return null;
    }
}
