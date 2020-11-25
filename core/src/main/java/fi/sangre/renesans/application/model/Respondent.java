package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.parameter.ParameterAnswer;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.model.respondent.RespondentState;
import lombok.*;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class Respondent {
    private RespondentId id;
    private String email;
    private Set<ParameterAnswer> parameterAnswers;
    private RespondentState state;
}
