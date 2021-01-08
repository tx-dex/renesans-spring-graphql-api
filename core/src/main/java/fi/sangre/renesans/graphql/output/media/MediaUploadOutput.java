package fi.sangre.renesans.graphql.output.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URL;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MediaUploadOutput {
    private String key;
    private URL url;
}
