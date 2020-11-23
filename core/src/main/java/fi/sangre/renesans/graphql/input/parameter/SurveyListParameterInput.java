package fi.sangre.renesans.graphql.input.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "value")
public class SurveyListParameterInput implements SurveyParameterInput {
    private String label;
    private String value;
    private List<SurveyParameterItemInput> children;
}
