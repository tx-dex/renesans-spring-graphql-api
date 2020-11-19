package fi.sangre.renesans.application.model.questions;

import fi.sangre.renesans.application.model.IdValueObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode(of = "value")
public class QuestionId implements IdValueObject<UUID> {
    private final UUID value;
}
