package fi.sangre.renesans.graphql.output.parameter;

import fi.sangre.renesans.application.model.SurveyParameterTypes;
import lombok.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@ToString
public class QuestionnaireListParameterOutput implements QuestionnaireParameterOutput {
    private final SurveyParameterTypes type = SurveyParameterTypes.LIST;
    private Map<String, String> labels;
    private String value;
    private List<QuestionnaireParameterItemOutput> children;
    private boolean answered;
    private Set<UUID> selectedAnswer;
}
