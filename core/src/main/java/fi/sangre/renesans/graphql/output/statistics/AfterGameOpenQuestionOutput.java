package fi.sangre.renesans.graphql.output.statistics;

import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Map;

@Data
@EqualsAndHashCode
@ToString
@Builder
public class AfterGameOpenQuestionOutput {
    private QuestionId id;
    private Map<String, String> titles;
    private Collection<OpenQuestionAnswerOutput> answers;
}
