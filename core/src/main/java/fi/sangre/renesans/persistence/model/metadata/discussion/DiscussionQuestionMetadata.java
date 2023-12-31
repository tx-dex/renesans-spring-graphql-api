package fi.sangre.renesans.persistence.model.metadata.discussion;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@ToString
@Builder
public class DiscussionQuestionMetadata implements Serializable {
    private UUID id;
    private Map<String, String> titles;
    private Boolean active;
    private Date createdDate;
}
