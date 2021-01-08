package fi.sangre.renesans.persistence.model.metadata.media;

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
        @JsonSubTypes.Type(value = ImageMetadata.class, name = "image"),
        @JsonSubTypes.Type(value = VideoMetadata.class, name = "video"),
        @JsonSubTypes.Type(value = FileMetadata.class, name = "file"),
})
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public interface MediaMetadata extends Serializable {
    String getKey();
    void setKey(String key);
}
