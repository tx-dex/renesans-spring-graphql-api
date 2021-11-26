package fi.sangre.renesans.graphql.output.dialogue;

import lombok.*;

import java.util.Collection;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class DialogueTopicOutput {
    private UUID id;
    private String title;
    private Integer questionsCount;
    private Integer sortOrder;
    private boolean active;

    // TODO: implement in the model layer
    private String picture = "";

    private Collection<DialogueQuestionOutput> questions;
    private Collection<DialogueTipOutput> tips;
}
