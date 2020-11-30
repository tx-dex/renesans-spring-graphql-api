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
public class QuestionnaireListParameterOutput implements QuestionnaireParameterOutput {
    private final SurveyParameterTypes type = SurveyParameterTypes.LIST;
    private Map<String, String> labels;
    private String value;
    private List<QuestionnaireParameterItemOutput> children;
    private boolean answered;
}
