package fi.sangre.renesans.graphql.input.media;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MediaParametersInput {
    private Long width;
    private Long height;
}
