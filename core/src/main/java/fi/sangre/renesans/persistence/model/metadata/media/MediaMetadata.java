package fi.sangre.renesans.persistence.model.metadata.media;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.io.Serializable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = ImageMetadata.class, name = "image"),
        @JsonSubTypes.Type(value = VideoMetadata.class, name = "video"),
        @JsonSubTypes.Type(value = PdfMetadata.class, name = "pdf"),
})
public interface MediaMetadata extends Serializable {
    String getKey();
    void setKey(String key);
}
