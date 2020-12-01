package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.Context;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.repository.QuestionGroupRepository;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.QuestionService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@RequiredArgsConstructor

@Component
@Deprecated
public class QuestionGroupResolver implements GraphQLResolver<QuestionGroup> {

    private final QuestionGroupRepository questionGroupRepository;
    private final MultilingualService multilingualService;
    private final QuestionService questionService;


    public String getTitle(QuestionGroup questionGroup, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(questionGroup.getTitleId(), getLanguageCode(environment));
    }

    public String getDescription(QuestionGroup questionGroup, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(questionGroup.getDescriptionId(), getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public String getPrescription(QuestionGroup questionGroup, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(questionGroup.getPrescriptionId(), getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getTitlePhraseId(QuestionGroup questionGroup) {
        return questionGroup.getTitleId();
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getDescriptionPhraseId(QuestionGroup questionGroup) {
        return questionGroup.getDescriptionId();
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Long getPrescriptionPhraseId(QuestionGroup questionGroup) {
        return questionGroup.getPrescriptionId();
    }

    public List<QuestionGroup> getChildren(QuestionGroup questionGroup) {
        return questionGroupRepository.findByParent(questionGroup);
    }

    public QuestionGroup getParent(QuestionGroup questionGroup) {
        return questionGroupRepository.findByChildren(questionGroup);
    }

    public List<Question> getQuestions(QuestionGroup questionGroup) {
        return questionService.getCatalystGenericQuestions(questionGroup.getId());
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public Double getWeight(QuestionGroup questionGroup) {
        return questionGroup.getWeight();
    }

    private String getLanguageCode(DataFetchingEnvironment environment) {
        Context context = environment.getContext();
        return Locale.forLanguageTag(context.getLanguageCode()).getLanguage();
    }
}
