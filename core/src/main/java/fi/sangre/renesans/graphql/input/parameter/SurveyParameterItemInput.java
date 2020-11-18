package fi.sangre.renesans.graphql.input.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class SurveyParameterItemInput {
    private String label;
    private String value;
}
