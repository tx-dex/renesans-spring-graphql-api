package fi.sangre.renesans.application.model.statistics;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.DriverId;
import fi.sangre.renesans.persistence.model.statistics.Statistics;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@Builder
@EqualsAndHashCode(of = "id")
public class DriverStatistics implements Statistics {
    public static final DriverStatistics EMPTY = DriverStatistics.builder()
            .weighedResult(null)
            .build();

    private DriverId id;
    private CatalystId catalystId;
    private Map<String, String> titles;
    private Double result;
    private Double weighedResult;
    private Double weight;
    private Double weightModifier;
    private Double importance;
    private Double rate;
}
