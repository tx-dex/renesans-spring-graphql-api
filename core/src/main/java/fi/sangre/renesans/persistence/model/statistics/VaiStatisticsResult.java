package fi.sangre.renesans.persistence.model.statistics;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class VaiStatisticsResult implements StatisticsResult {

    private final Double result;

    @Override
    public Double getWeighedResult() {
        return result;
    }

    @Override
    public Double getRate() {
        return null;
    }
}
