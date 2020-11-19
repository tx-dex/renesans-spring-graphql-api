package fi.sangre.renesans.persistence.model.metadata.references;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.persistence.model.TemplateId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TemplateReference implements MetadataReference {
    private TemplateId id;
    private Long version;
}
