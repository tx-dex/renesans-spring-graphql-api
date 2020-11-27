package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireOpenQuestionOutput;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireCatalystOutput {
    private UUID id;
    private MultilingualText titles;
    private List<QuestionnaireLikertQuestionOutput> questions;
    private List<QuestionnaireDriverOutput> drivers;
    private QuestionnaireOpenQuestionOutput catalystQuestion;
}
