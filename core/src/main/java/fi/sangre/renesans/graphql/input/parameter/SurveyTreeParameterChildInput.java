package fi.sangre.renesans.graphql.input.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class SurveyTreeParameterChildInput {
    private String label;
    private String value;
    private List<SurveyTreeParameterChildInput> children;
}
