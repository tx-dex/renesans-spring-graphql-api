package fi.sangre.renesans.application.model.media;

import fi.sangre.renesans.application.model.MediaId;
import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@ToString
@Builder
public class Media {
    private MediaId id;
    private MultilingualText title;
    private MediaType type;
    private MediaDetails details;
}
