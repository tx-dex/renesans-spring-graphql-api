package fi.sangre.renesans.application.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "value")
public class TemplateId implements IdValueObject<Long> {
    private final Long value;

    @Override
    public String asString() {
        return value.toString();
    }
}
