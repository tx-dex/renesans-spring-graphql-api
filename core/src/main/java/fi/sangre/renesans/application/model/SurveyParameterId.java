package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public class SurveyParameterId {
    private final SurveyId surveyId;
    private final ParameterId parameterId;

    public SurveyParameterId(final SurveyId surveyId, final ParameterId parameterId) {
        this.surveyId = Objects.requireNonNull(surveyId, "Survey id is required");
        this.parameterId = Objects.requireNonNull(parameterId, "Parameter id is required");
    }

}
