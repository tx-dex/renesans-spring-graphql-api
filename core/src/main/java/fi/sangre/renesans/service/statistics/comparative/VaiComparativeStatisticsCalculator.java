package fi.sangre.renesans.service.statistics.comparative;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.CatalystStatistics;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;
import fi.sangre.renesans.persistence.model.statistics.VaiStatisticsResult;
import fi.sangre.renesans.service.StatisticsService;

import java.util.List;
import java.util.Map;

public class VaiComparativeStatisticsCalculator implements ComparativeStatisticsCalculator {
    private final OrganizationSurvey survey;
    private final StatisticsService statisticsService;

    public VaiComparativeStatisticsCalculator(OrganizationSurvey survey, StatisticsService statisticsService) {
        this.survey = survey;
        this.statisticsService = statisticsService;
    }

    @Override
    public StatisticsResult getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics) {
        final List<DriverStatistics> driverStatistics = statisticsService.calculateDriversStatistics(survey, questionStatistics);
        final List<CatalystStatistics> catalystsStatistics = statisticsService.calculateCatalystsStatistics(survey, driverStatistics, questionStatistics);
        Double vai = statisticsService.calculateVisionAttainmentIndicator(catalystsStatistics);

        return new VaiStatisticsResult(vai);
    }

    @Override
    public String getLabel(String languageCode) {
        return null;
    }
}
