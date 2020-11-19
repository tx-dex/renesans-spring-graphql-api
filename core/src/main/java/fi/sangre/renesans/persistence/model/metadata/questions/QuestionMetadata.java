package fi.sangre.renesans.persistence.model.metadata.questions;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import fi.sangre.renesans.persistence.model.metadata.references.MetadataReference;

import java.io.Serializable;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = LikertQuestionMetadata.class, name = "likert"),
})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public interface QuestionMetadata extends Serializable {
    UUID getId();
    MetadataReference getReference();
}
