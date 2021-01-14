package fi.sangre.renesans.application.model.statistics;

import com.google.common.collect.Maps;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.statistics.StatisticsQuestion;
import lombok.Builder;
import lombok.Data;

import java.util.List;
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
    private List<StatisticsQuestion> questions;
}
