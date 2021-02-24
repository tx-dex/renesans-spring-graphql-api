package fi.sangre.renesans.application.model.questions;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class OpenQuestion {
    private QuestionId id;
    private CatalystId catalystId;
    private MultilingualText titles;
}
