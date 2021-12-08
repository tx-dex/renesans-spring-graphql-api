package fi.sangre.renesans.graphql.output.dialogue;

import lombok.*;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class DialogueCommentOutput {
    private UUID id;
    private Integer likesCount;
    private String createdAt;
    private String text;
    private boolean hasLikeByThisRespondent;
    private Collection<DialogueCommentOutput> replies;
    private String respondentColor;
    private UUID authorRespondentId;
    private boolean isOwnedByThisRespondent;
}
