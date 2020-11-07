package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.service.StatisticsService;
import fi.sangre.renesans.statistics.StatisticsQuestion;
import fi.sangre.renesans.statistics.StatisticsQuestionsRank;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@Component
public class StatisticsQuestionsRankResolver implements GraphQLResolver<StatisticsQuestionsRank> {

    public List<StatisticsQuestion> getTop(final StatisticsQuestionsRank rank, final Integer size) {
        return rank.getQuestionStatistics().stream()
                .filter(e -> Objects.nonNull(e.getAnswer().getAvg()))
                .sorted(StatisticsService.QUESTION_COMPARATOR.reversed())
                .limit(size)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public List<StatisticsQuestion> getLast(final StatisticsQuestionsRank rank, final Integer size) {
        return rank.getQuestionStatistics().stream()
                .filter(e -> Objects.nonNull(e.getAnswer().getAvg()))
                .sorted(StatisticsService.QUESTION_COMPARATOR)
                .limit(size)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
