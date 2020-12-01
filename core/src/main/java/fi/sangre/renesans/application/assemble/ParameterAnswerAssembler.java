package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.parameter.ParameterChild;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.answer.ParameterAnswerInput;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

@RequiredArgsConstructor
@Slf4j

@Component
public class ParameterAnswerAssembler {
    private final SurveyUtils surveyUtils;
    private final ParameterUtils parameterUtils;

    public Parameter fromInput(@NonNull final ParameterAnswerInput input, @NonNull final OrganizationSurvey survey) {
        checkArgument(input.getParameterId() != null, "input.parameterId is required");

        final ParameterId parameterId = new ParameterId(input.getParameterId());
        final Parameter parameter = surveyUtils.findParameter(parameterId, survey);

        if (parameter != null) {
            final ParameterId childId = new ParameterId(input.getValue());
            final ParameterChild child = parameterUtils.findChild(childId, parameter);

            if (child != null) {
                return child;
            } else {
                throw new SurveyException("Child not found in the parameter");
            }
        } else {
            throw new SurveyException("Parameter not found in the survey");
        }
    }

    @NonNull
    public Parameter fromEntity(@NonNull final List<ParameterAnswerEntity> answers, @NonNull final OrganizationSurvey survey) {
        final ParameterAnswerEntity item = answers.stream()
                .filter(e -> ParameterAnswerType.ITEM.equals(e.getType()))
                .findFirst()
                .orElseThrow(() -> new SurveyException("Parameter answer not found"));

        final ParameterId childId = new ParameterId(item.getId().getParameterId());
        final ParameterId rootId = new ParameterId(item.getRootId());
        final Parameter root = Objects.requireNonNull(surveyUtils.findParameter(rootId, survey), "Parameter not found in the survey");

        return Objects.requireNonNull(parameterUtils.findChild(childId, root), "Child not found in the root parameter");
    }
}
