package fi.sangre.renesans.graphql.input.discussion;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@NoArgsConstructor
@Data
@ToString
public class DiscussionQuestionInput {
    private UUID id;
    private String title;
    private Boolean active;
}
