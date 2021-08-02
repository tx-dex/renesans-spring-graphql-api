package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.StatisticsService;
import fi.sangre.renesans.statistics.StatisticsQuestion;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static fi.sangre.renesans.application.utils.StatisticsUtils.MAX_ANSWER_VALUE;

@Deprecated
@RequiredArgsConstructor
@Component
public class StatisticsQuestionResolver implements GraphQLResolver<StatisticsQuestion> {
    private final MultilingualService multilingualService;
    private final ResolverHelper helper;

    public String getTitle(final StatisticsQuestion question, final DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(question.getTitleId(), helper.getLanguageCode(environment));
    }

    public Integer getMax(final StatisticsQuestion question) {
        return question.getAnswer().getMax();
    }

    public Integer getMin(final StatisticsQuestion question) {
        return question.getAnswer().getMin();
    }

    public Double getAvg(final StatisticsQuestion question) {
        return question.getAnswer().getAvg() != null ? question.getAnswer().getAvg() / MAX_ANSWER_VALUE : null;
    }

    public Long getCount(final StatisticsQuestion question) {
        return question.getAnswer().getCount();
    }
}
