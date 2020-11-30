package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireOutput {
    private UUID id;
    private List<StaticText> staticTexts;
    private List<QuestionnaireCatalystOutput> catalysts;
    private List<QuestionnaireParameterOutput> parameters;
    boolean finished;
    boolean answerable;
}
