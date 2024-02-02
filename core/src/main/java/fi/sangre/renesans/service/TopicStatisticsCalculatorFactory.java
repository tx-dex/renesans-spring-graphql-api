package fi.sangre.renesans.service;

import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.SurveyUtils;

import java.util.UUID;

public class TopicStatisticsCalculatorFactory {
    public static TopicStatisticsCalculator getCalculator(TopicType topicType,
                                                          String topicId,
                                                          OrganizationSurvey survey,
                                                          SurveyUtils surveyUtils,
                                                          StatisticsService statisticsService) throws Exception {
        switch (topicType) {
            case STATEMENT:
                LikertQuestion question = surveyUtils.findQuestion(new QuestionId(UUID.fromString(topicId)), survey);
                return new StatementTopicStatisticCalculator(question);
            case DRIVER:
                Driver driver = surveyUtils.findDriver(Long.parseLong(topicId), survey);
                return new DriverTopicStatisticCalculator(driver, survey, statisticsService);
            case THEME:
                Catalyst catalyst = surveyUtils.findCatalyst(new CatalystId(UUID.fromString(topicId)), survey);
                return new ThemeTopicStatisticCalculator(catalyst, survey, statisticsService);
            default:
                throw new Exception("Unexpected topic type " + topicType);
        }
    }
}
