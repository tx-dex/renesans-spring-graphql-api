package fi.sangre.renesans.persistence.model.metadata.media;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class ImageMetadata implements MediaMetadata {
    private String key;
}
