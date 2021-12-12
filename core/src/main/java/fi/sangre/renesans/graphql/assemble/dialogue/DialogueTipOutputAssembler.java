package fi.sangre.renesans.graphql.assemble.dialogue;

import fi.sangre.renesans.graphql.output.dialogue.DialogueTipOutput;
import fi.sangre.renesans.persistence.dialogue.model.DialogueTipEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

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
    public Collection<DialogueTipOutput> from(Set<DialogueTipEntity> entitySet) {
        Collection<DialogueTipOutput> outputs = new ArrayList<>();

        entitySet.forEach(entity -> {
            outputs.add(from(entity));
        });

        return outputs;
    }
}
