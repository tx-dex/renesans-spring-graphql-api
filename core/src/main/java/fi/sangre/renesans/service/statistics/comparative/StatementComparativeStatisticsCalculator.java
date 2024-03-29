package fi.sangre.renesans.service.statistics.comparative;

import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;
import org.springframework.beans.factory.annotation.Configurable;

import java.util.Map;

@Configurable(preConstruction = true)
public class StatementComparativeStatisticsCalculator implements ComparativeStatisticsCalculator {

    private final LikertQuestion question;

    public StatementComparativeStatisticsCalculator(LikertQuestion question) {
        this.question = question;
    }

    public StatisticsResult getStatistics(Map<QuestionId, QuestionStatistics> questionStatistics) {
        return questionStatistics.get(question.getId());
    }

    public String getLabel(String languageCode) {
        return question.getTitles().getPhrase(languageCode);
    }
}
