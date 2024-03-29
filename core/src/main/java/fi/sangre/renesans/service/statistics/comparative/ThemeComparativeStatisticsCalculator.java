package fi.sangre.renesans.service.statistics.comparative;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;
import fi.sangre.renesans.service.StatisticsService;

import java.util.List;
import java.util.Map;

public class ThemeComparativeStatisticsCalculator implements ComparativeStatisticsCalculator {

    private final Catalyst catalyst;
    private final OrganizationSurvey survey;
    private final StatisticsService statisticsService;

    public ThemeComparativeStatisticsCalculator(Catalyst catalyst, OrganizationSurvey survey, StatisticsService statisticsService) {
        this.catalyst = catalyst;
        this.survey = survey;
        this.statisticsService = statisticsService;
    }

    @Override
    public StatisticsResult getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics) {
        List<DriverStatistics> driverStatistics = statisticsService.calculateDriversStatistics(survey, questionStatistics);

        return statisticsService.calculateCatalystListStatistics(ImmutableList.of(catalyst),
                driverStatistics,
                questionStatistics).get(0);
    }

    @Override
    public String getLabel(String languageCode) {
        return catalyst.getTitles().getPhrase(languageCode);
    }
}
