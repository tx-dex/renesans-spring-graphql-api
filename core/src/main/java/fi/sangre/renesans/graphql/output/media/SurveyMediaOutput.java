package fi.sangre.renesans.graphql.output.media;

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
    private String key;
}
