package fi.sangre.renesans.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@NoArgsConstructor
@Data

@ConfigurationProperties(prefix = "fi.sangre.renesans.statistics")
public class StatisticsProperties {
    private Integer minRespondentCount;
}
