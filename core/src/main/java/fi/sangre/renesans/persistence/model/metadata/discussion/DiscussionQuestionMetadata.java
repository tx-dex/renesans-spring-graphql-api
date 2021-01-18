package fi.sangre.renesans.persistence.model.metadata.discussion;

import lombok.*;

import java.io.Serializable;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class DiscussionQuestionMetadata implements Serializable {
    private UUID id;
    private Map<String, String> titles;
    private Boolean active;
}
