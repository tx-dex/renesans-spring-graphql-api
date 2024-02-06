package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;

@Configurable(preConstruction = true)
public class DriverTopicStatisticCalculator implements TopicStatisticsCalculator {

    private final StatisticsService statisticsService;
    private final Driver driver;
    private final Map<QuestionId, Map<DriverId, Double>> questionWeights;

    public DriverTopicStatisticCalculator(Driver driver, OrganizationSurvey survey, StatisticsService statisticsService) {
        this.driver = driver;
        this.statisticsService = statisticsService;
        questionWeights = statisticsService.getQuestionWeights(survey);
    }

    public StatisticsResult getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics) {
        return statisticsService.calculateDriversStatistics(ImmutableList.of(driver),
                questionWeights,
                questionStatistics).get(0);
    }

    public String getLabel(String languageCode) {
        return driver.getTitles().getPhrase(languageCode);
    }
}
