package fi.sangre.renesans.graphql.input.parameter;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SurveyListParameterInput implements SurveyParameterInput {
    private String label;
    private String value;
    private List<SurveyParameterItemInput> children;
}
