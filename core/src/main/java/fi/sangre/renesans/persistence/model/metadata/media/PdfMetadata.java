package fi.sangre.renesans.persistence.model.metadata.media;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

import java.util.Map;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode(of = "id")
@Builder

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PdfMetadata implements MediaMetadata {
    private UUID id;
    private Map<String, String> titles;
    private String key;
}
