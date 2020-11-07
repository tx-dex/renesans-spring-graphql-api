package fi.sangre.renesans.dto;

import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireDto {
    private String id;
    private Respondent respondent;
    private RespondentGroup respondentGroup;
}
