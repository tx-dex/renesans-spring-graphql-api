package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.service.MultilingualService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
@Deprecated
public class SurveyResolver implements GraphQLResolver<Survey> {
    private final MultilingualService multilingualService;
    private final ResolverHelper helper;

    public String getTitle(Survey survey, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(survey.getTitleId(), helper.getLanguageCode(environment));
    }

    public String getDescription(Survey survey, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(survey.getDescriptionId(), helper.getLanguageCode(environment));
    }

    // do not expose internals to the public
    public Long getTitlePhraseId(Survey survey) {
        return survey.getTitleId();
    }

    // do not expose internals to the public
    public Long getDescriptionPhraseId(Survey survey) {
        return survey.getDescriptionId();
    }

    public List<RespondentGroup> getRespondentGroups(Survey survey) {
        return ImmutableList.of();
    }

    public List<QuestionGroup> getQuestionGroups(Survey survey) {
        return ImmutableList.of();
    }

    public List<CatalystProxy> getCatalysts(final Survey survey, final DataFetchingEnvironment environment) {
        return ImmutableList.of();
    }
}
