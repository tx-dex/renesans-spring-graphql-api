package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.statistics.StatisticsDriver;
import org.springframework.stereotype.Component;

@Component
public class StatisticsDriverResolver implements GraphQLResolver<StatisticsDriver> {
    public Double getIndex(final StatisticsDriver driver) {
        return driver.getResult();
    }

    public Double getWeighedIndex(final StatisticsDriver driver) {
        return driver.getWeighedResult();
    }
}
