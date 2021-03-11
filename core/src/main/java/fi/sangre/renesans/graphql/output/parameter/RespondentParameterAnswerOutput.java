package fi.sangre.renesans.graphql.output.parameter;

import fi.sangre.renesans.application.model.ParameterId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class RespondentParameterAnswerOutput {
    private ParameterId rootId;
    private String response;
}
