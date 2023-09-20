package fi.sangre.renesans.graphql.assemble.statistics;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.statistics.DetailedDriverStatistics;
import fi.sangre.renesans.graphql.output.statistics.AfterGameDetailedDriverStatisticsOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;

import static fi.sangre.renesans.application.utils.StatisticsUtils.rateToPercent;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameDetailedDriversStatisticsAssembler {
    private final AfterGameQuestionsStatisticsAssembler afterGameQuestionsStatisticsAssembler;

    @NonNull
    public Collection<AfterGameDetailedDriverStatisticsOutput> from(
            @NonNull final Collection<DetailedDriverStatistics> detailedDriverStatisticsList,
            @NonNull final OrganizationSurvey survey
            ) {
        Collection<AfterGameDetailedDriverStatisticsOutput> outputs = new ArrayList<>();

        detailedDriverStatisticsList.forEach((detailedDriverStatistics) -> {
            outputs.add(AfterGameDetailedDriverStatisticsOutput.builder()
                    .titles(detailedDriverStatistics.getTitles())
                    .catalyst(detailedDriverStatistics.getCatalyst())
                    .result(rateToPercent(detailedDriverStatistics.getResult()))
                    .rate(detailedDriverStatistics.getRate())
                    .questionsStatistics(
                            afterGameQuestionsStatisticsAssembler.from(
                                    detailedDriverStatistics.getQuestionsStatistics(),
                                    survey
                            )
                    )
                    .build());
        });

        return outputs;
    }
}
