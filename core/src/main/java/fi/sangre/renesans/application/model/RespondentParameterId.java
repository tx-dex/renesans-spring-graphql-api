package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;

@Getter
@EqualsAndHashCode
public class RespondentParameterId {
    private final RespondentId respondentId;
    private final ParameterId parameterId;

    public RespondentParameterId(final RespondentId respondentId, final ParameterId parameterId) {
        this.respondentId = Objects.requireNonNull(respondentId, "Respondent id is required");
        this.parameterId = Objects.requireNonNull(parameterId, "Parameter id is required");
    }

}
