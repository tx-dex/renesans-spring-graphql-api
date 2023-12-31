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
public class QuestionnaireParameterItemOutput implements QuestionnaireParameterChildOutput {
    private String value;
    private Map<String, String> labels;
    private boolean checked;
    @Builder.Default
    private boolean selectable = true;
}
