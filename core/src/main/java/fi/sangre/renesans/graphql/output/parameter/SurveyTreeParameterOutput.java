package fi.sangre.renesans.graphql.output.parameter;

import fi.sangre.renesans.application.model.SurveyParameterTypes;
import lombok.*;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class SurveyTreeParameterOutput implements SurveyParameterOutput, SurveyParameterChildOutput {
    private final SurveyParameterTypes type = SurveyParameterTypes.TREE;
    private String value;
    private Map<String, String> labels;
    private List<SurveyParameterChildOutput> children;
}
