package fi.sangre.renesans.application.model.statistics;

import com.google.common.collect.Maps;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CatalystStatistics {
    private CatalystId id;
    private Map<String, String> titles;

    private Double result;
    private Double weighedResult;
    private Double weight;
    private Double importance;
    @Builder.Default
    private Map<DriverId, DriverStatistics> drivers = Maps.newHashMap();
    @Builder.Default
    private Map<QuestionId, QuestionStatistics> questions = Maps.newHashMap();
}
