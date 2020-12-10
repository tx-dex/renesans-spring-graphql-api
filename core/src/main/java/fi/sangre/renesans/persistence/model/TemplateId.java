package fi.sangre.renesans.persistence.model;

import fi.sangre.renesans.application.model.IdValueObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "value")
public class TemplateId implements IdValueObject<Long> {
    private  Long value;

    @Override
    public String asString() {
        return value.toString();
    }
}
