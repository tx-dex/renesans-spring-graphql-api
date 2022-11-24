package fi.sangre.renesans.graphql.output.statistics;

import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameOpenQuestionStatisticsOutput {
    private UUID questionId;
    private Map<String, String> titles;
    private Collection<AfterGameOpenQuestionAnswerOutput> answers;
    private Map<String, String> catalystTitles;

    public void addAnswer(AfterGameOpenQuestionAnswerOutput answerOutput) {
        this.answers.add(answerOutput);
    }
}
