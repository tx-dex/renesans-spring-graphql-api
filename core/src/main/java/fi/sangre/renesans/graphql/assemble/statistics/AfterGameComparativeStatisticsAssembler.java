package fi.sangre.renesans.graphql.assemble.statistics;

import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.TopicType;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.graphql.output.statistics.AfterGameComparativeStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.ParameterStatisticOutput;
import fi.sangre.renesans.persistence.model.statistics.StatisticsResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static fi.sangre.renesans.application.utils.StatisticsUtils.rateToPercent;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j
@Component
public class AfterGameComparativeStatisticsAssembler {

    private final ParameterUtils parameterUtils;

    private ParameterStatisticOutput from(Parameter parameter, StatisticsResult statisticsResult, String languageCode) {
        List<String> parents = parameterUtils.getAllParents(parameter)
                .stream().map(parent -> parent.getLabel().getPhrase(languageCode))
                .collect(toList());

        return ParameterStatisticOutput.builder()
                .label(parameter.getLabel().getPhrase(languageCode))
                .parents(parents)
                .result(statisticsResult != null
                        ? rateToPercent(statisticsResult.getWeighedResult())
                        : null)
                .rate(statisticsResult != null
                        ? statisticsResult.getRate()
                        : null)
                .build();
    }

    public AfterGameComparativeStatisticsOutput from(String topicLabel,
                                                     TopicType topicType,
                                                     StatisticsResult totalStatisticsResult,
                                                     List<Parameter> parameters,
                                                     Map<ParameterId, StatisticsResult> parameterStatistics,
                                                     String languageCode) {
        List<ParameterStatisticOutput> parameterOutputs = parameters.stream().map(parameter -> from(parameter, parameterStatistics.get(parameter.getId()), languageCode)).collect(toList());

        return AfterGameComparativeStatisticsOutput.builder()
                .topic(topicLabel)
                .type(topicType.name())
                .totalResult(totalStatisticsResult != null
                        ? rateToPercent(totalStatisticsResult.getWeighedResult())
                        : null)
                .totalRate(totalStatisticsResult != null
                        ? totalStatisticsResult.getRate()
                        :null)
                .parameters(parameterOutputs)
                .build();
    }
}
