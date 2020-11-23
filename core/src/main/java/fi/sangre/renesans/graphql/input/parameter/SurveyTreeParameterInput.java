package fi.sangre.renesans.graphql.input.parameter;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "value")
@Builder
public class SurveyTreeParameterInput implements SurveyParameterInput {
    private String label;
    private String value;
    private List<SurveyTreeParameterChildInput> children;
}
