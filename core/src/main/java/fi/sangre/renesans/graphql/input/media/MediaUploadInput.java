package fi.sangre.renesans.graphql.input.media;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class MediaUploadInput {
    private String fileName;
    private String mimeType;
}
