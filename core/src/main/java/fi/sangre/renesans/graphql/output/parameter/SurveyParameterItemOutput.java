package fi.sangre.renesans.graphql.output.parameter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class SurveyParameterItemOutput implements SurveyParameterChildOutput {
    private String value;
    private Map<String, String> labels;
}
