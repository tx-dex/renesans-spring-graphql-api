package fi.sangre.renesans.graphql.input.parameter;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
@EqualsAndHashCode(of = "value")
public class SurveyParameterItemInput {
    private String label;
    private String value;
}
