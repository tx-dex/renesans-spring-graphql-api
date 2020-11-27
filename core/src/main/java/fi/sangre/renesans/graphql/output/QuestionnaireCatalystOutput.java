package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.graphql.output.question.QuestionnaireQuestionOutput;
import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireCatalystOutput {
    private Long id;
    private MultilingualText titles;
    private List<QuestionnaireQuestionOutput> questions;
    private List<QuestionnaireDriverOutput> drivers;
}
