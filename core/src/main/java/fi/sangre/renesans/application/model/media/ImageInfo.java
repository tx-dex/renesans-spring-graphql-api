package fi.sangre.renesans.application.model.media;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString
@EqualsAndHashCode
@Builder
public class ImageInfo {
    private String key;
}
