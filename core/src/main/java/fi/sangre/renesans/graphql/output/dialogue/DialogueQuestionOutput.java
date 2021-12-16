package fi.sangre.renesans.graphql.output.dialogue;

import lombok.*;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class DialogueQuestionOutput {
    private UUID id;
    private String title;
    private Integer answersCount;
    private Integer likesCount;
    private boolean hasLikeByThisRespondent;
    private boolean active;

    // TODO: implement in the model layer later
    private String image = "";

    private Collection<DialogueCommentOutput> comments;
}
