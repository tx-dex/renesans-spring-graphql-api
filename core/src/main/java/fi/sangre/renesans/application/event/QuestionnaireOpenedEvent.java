package fi.sangre.renesans.application.event;

import fi.sangre.renesans.application.model.IdValueObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class QuestionnaireOpenedEvent {
    private final IdValueObject<UUID> respondentId;
}
