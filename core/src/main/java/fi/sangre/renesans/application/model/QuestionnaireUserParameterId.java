package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Objects;
import java.util.UUID;

@Getter
@EqualsAndHashCode
public class QuestionnaireUserParameterId {
    private final IdValueObject<UUID> userId;
    private final ParameterId parameterId;

    public QuestionnaireUserParameterId(final IdValueObject<UUID> userId, final ParameterId parameterId) {
        this.userId = Objects.requireNonNull(userId, "UserId is required");
        this.parameterId = Objects.requireNonNull(parameterId, "Parameter id is required");
    }

}
