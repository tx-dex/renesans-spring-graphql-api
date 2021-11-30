package fi.sangre.renesans.graphql.assemble.dialogue;

import fi.sangre.renesans.graphql.output.dialogue.DialogueTipOutput;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTipEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class DialogueTipOutputAssembler {
    @NonNull
    public DialogueTipOutput from(DialogueTipEntity entity) {
        return DialogueTipOutput.builder()
                .id(entity.getId())
                .text(entity.getText())
                .build();
    }

    @NonNull
    public Collection<DialogueTipOutput> from(Map<UUID, DialogueTipEntity> entityMap) {
        Collection<DialogueTipOutput> outputs = new ArrayList<>();

        entityMap.values().forEach(entity -> {
            outputs.add(from(entity));
        });

        return outputs;
    }
}
