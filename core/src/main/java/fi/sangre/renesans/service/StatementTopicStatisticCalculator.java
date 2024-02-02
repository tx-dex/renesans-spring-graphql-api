package fi.sangre.renesans.service;

import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.Statistics;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;

@Configurable(preConstruction = true)
public class StatementTopicStatisticCalculator implements TopicStatisticsCalculator {

    private final LikertQuestion question;

    public StatementTopicStatisticCalculator(LikertQuestion question) {
        this.question = question;
    }

    public Statistics getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics) {
        return questionStatistics.get(question.getId());
    }

    public String getLabel(String languageCode) {
        return question.getTitles().getPhrase(languageCode);
    }
}
