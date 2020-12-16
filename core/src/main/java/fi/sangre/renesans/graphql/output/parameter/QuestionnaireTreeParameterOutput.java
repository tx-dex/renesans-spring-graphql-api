package fi.sangre.renesans.graphql.output.parameter;

import fi.sangre.renesans.application.model.SurveyParameterTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class QuestionnaireTreeParameterOutput implements QuestionnaireParameterOutput, QuestionnaireParameterChildOutput {
    private final SurveyParameterTypes type = SurveyParameterTypes.TREE;
    private String value;
    private Map<String, String> labels;
    private List<QuestionnaireParameterChildOutput> children;
    private boolean answered;
    @Builder.Default
    private boolean selectable = false;
}
