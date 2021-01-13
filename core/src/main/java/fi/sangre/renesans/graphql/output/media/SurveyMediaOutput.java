package fi.sangre.renesans.graphql.output.media;

import fi.sangre.renesans.application.model.media.MediaType;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class SurveyMediaOutput {
    private UUID id;
    private Map<String, String> titles;
    private MediaType type;
    private String key;
}
