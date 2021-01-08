package fi.sangre.renesans.persistence.model.metadata.media;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
public class VideoMetadata implements MediaMetadata {
    private String key;
}
