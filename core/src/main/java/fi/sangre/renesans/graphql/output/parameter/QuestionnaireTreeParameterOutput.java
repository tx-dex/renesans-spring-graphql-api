package fi.sangre.renesans.graphql.output.parameter;

import fi.sangre.renesans.application.model.SurveyParameterTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private Set<UUID> selectedAnswer;
}
