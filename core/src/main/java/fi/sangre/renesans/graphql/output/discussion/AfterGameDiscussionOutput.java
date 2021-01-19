package fi.sangre.renesans.graphql.output.discussion;

import lombok.*;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class AfterGameDiscussionOutput {
    private UUID id;
    private Map<String, String> titles;
    private boolean active;
    private Long numberOfAllComments;
}
