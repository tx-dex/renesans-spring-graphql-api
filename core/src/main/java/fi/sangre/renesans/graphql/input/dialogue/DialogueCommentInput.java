package fi.sangre.renesans.graphql.input.dialogue;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@Data
@ToString
public class DialogueCommentInput {
    private String text;
    private String mediaAttachment;
}
