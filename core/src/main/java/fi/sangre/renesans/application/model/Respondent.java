package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
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
    private SurveyId surveyId;
    private String email;
    private Set<ParameterItemAnswer> parameterAnswers;
    private RespondentState state;
}
