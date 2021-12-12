package fi.sangre.renesans.graphql.input.dialogue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@NoArgsConstructor
@Data
@ToString
public class DialogueTipInput {
    private UUID id;
    private String text;
}
