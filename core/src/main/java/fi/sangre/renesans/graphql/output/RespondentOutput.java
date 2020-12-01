package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.respondent.RespondentState;
import fi.sangre.renesans.graphql.output.parameter.RespondentParameterAnswerOutput;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class RespondentOutput {
    private RespondentId id;
    private String email;
    private List<RespondentParameterAnswerOutput> parameterAnswers;
    private RespondentState state;
}

