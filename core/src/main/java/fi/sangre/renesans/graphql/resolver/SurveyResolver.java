package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.repository.QuestionGroupRepository;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.QuestionService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class SurveyResolver implements GraphQLResolver<Survey> {
    private final RespondentGroupRepository respondentGroupRepository;
    private final QuestionGroupRepository questionGroupRepository;
    private final MultilingualService multilingualService;
    private final QuestionService questionService;
    private final ResolverHelper helper;

    public String getTitle(Survey survey, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(survey.getTitleId(), helper.getLanguageCode(environment));
    }

    @PreAuthorize("isAuthenticated()")
    public String getDescription(Survey survey, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(survey.getDescriptionId(), helper.getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getTitlePhraseId(Survey survey) {
        return survey.getTitleId();
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getDescriptionPhraseId(Survey survey) {
        return survey.getDescriptionId();
    }

    @PreAuthorize("isAuthenticated()")
    public List<RespondentGroup> getRespondentGroups(Survey survey) {
        if (survey != null) {
            return respondentGroupRepository.findBySurvey(survey);
        }
        return null;
    }

    @Deprecated
    public List<QuestionGroup> getQuestionGroups(Survey survey) {
        return questionGroupRepository.findBySurveyAndParentIsNull(survey);
    }

    public List<CatalystDto> getCatalysts(final Survey survey, final DataFetchingEnvironment environment) {
        return questionService.getCatalysts(survey);
    }
}
