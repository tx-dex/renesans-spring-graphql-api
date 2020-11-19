package fi.sangre.renesans.persistence.model.metadata.references;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TemplateReference.class, name = "template"),
})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public interface MetadataReference extends Serializable {

}
