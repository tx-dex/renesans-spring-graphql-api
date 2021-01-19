package fi.sangre.renesans.graphql.output.discussion;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class AfterGameCommentOutput {
    private UUID id;
    private String text;
    private boolean liked;
    private Long numberOfAllLikes;
    private String author;
}
