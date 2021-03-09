package fi.sangre.renesans.persistence.model.answer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode

@Embeddable
public class GuestParameterAnswerId implements Serializable {
    @Column(name = "survey_id", updatable = false, nullable = false)
    private UUID surveyId;

    @Column(name = "guest_id", updatable = false, nullable = false)
    private UUID guestId;

    @Column(name = "parameter_id", columnDefinition = "uuid")
    private UUID parameterId;
}
