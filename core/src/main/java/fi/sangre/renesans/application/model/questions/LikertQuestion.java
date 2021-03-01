package fi.sangre.renesans.application.model.questions;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class LikertQuestion {
    private QuestionId id;
    private CatalystId catalystId;
    private MultilingualText titles;
    private MultilingualText subTitles;
    private MultilingualText lowEndLabels;
    private MultilingualText highEndLabels;
    private Map<DriverId, Double> weights;
}
