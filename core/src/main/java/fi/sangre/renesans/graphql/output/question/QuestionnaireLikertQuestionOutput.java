package fi.sangre.renesans.graphql.output.question;

import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.*;

import java.util.Map;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireLikertQuestionOutput implements QuestionnaireQuestionOutput {
    private QuestionId id;
    private Map<String, String> titles;
    private boolean skipped;
    private boolean answered;
    private Long index;
}
