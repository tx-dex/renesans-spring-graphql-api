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
public class ParameterAnswerId implements Serializable {
    @Column(name = "survey_id", updatable = false, nullable = false)
    private UUID surveyId;

    @Column(name = "respondent_id", updatable = false, nullable = false)
    private UUID respondentId;

    @Column(name = "parameter_id", columnDefinition = "uuid")
    private UUID parameterId;
}
