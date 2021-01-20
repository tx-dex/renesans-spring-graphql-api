package fi.sangre.renesans.graphql.input.discussion;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.UUID;

@NoArgsConstructor
@Data
@ToString
public class DiscussionCommentInput {
    private UUID commentId;
    private String text;
}
