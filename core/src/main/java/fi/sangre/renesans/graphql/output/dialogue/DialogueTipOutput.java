package fi.sangre.renesans.graphql.output.dialogue;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class DialogueTipOutput {
    private UUID id;
    private String text;
}
