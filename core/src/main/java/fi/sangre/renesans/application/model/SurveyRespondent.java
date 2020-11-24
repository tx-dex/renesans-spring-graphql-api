package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.parameter.ParameterAnswer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
public class SurveyRespondent {
    private RespondentId id;
    private String email;
    private Set<ParameterAnswer> parameterAnswers;
    private SurveyRespondentState state;
}
