package fi.sangre.renesans.application.model.answer;

import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.SurveyParameterId;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "rootId")
@Builder
public class ParameterItemAnswer {
    private SurveyParameterId rootId;
    private ParameterId response;
}
