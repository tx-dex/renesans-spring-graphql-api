package fi.sangre.renesans.persistence.model.metadata.parameters;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = TreeParameterMetadata.class, name = "tree"),
        @JsonSubTypes.Type(value = ParameterItemMetadata.class, name = "child"),
})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public interface ParameterChildMetadata extends Serializable {
    @NonNull
    UUID getId();
}
