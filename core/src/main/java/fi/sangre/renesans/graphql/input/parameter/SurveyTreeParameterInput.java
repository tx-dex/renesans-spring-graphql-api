package fi.sangre.renesans.graphql.input.parameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SurveyTreeParameterInput implements SurveyParameterInput {
    private String label;
    private String value;
    private List<SurveyTreeParameterChildInput> children;
}
