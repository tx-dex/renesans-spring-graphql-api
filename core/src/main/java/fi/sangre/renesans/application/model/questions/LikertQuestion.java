package fi.sangre.renesans.application.model.questions;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.DriverWeight;
import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class LikertQuestion {
    private QuestionId id;
    private CatalystId catalystId;
    private MultilingualText titles;
    private List<DriverWeight> weights;
}
