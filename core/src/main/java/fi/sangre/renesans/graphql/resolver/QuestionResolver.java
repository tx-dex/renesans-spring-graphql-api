package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.aaa.UserPrincipalService;
import fi.sangre.renesans.model.AnswerOption;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Weight;
import fi.sangre.renesans.repository.WeightRepository;
import fi.sangre.renesans.service.AnswerOptionService;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.QuestionService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class QuestionResolver implements GraphQLResolver<Question> {
    private final WeightRepository weightRepository;
    private final MultilingualService multilingualService;
    private final AnswerOptionService answerOptionService;
    private final QuestionService questionService;
    private final UserPrincipalService userPrincipalService;
    private final ResolverHelper helper;

    public String getTitle(Question question, DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(question.getTitleId(), helper.getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Long getTitlePhraseId(final Question question) {
        return question.getTitleId();
    }

    public List<AnswerOption> getAnswerOptions(Question question) {
        return ImmutableList.of();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Question.QuestionType getQuestionType(final Question question) {
        return question.getQuestionType();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public List<Weight> getWeights(final Question question) {
        final UserPrincipal user = userPrincipalService.getLoggedInPrincipal();
        if (userPrincipalService.isSuperUser(user)) {
            return ImmutableList.copyOf(weightRepository.findAllByQuestion(question));
        } else if (question.getSourceType() == Question.SourceType.ORGANISATION
            && userPrincipalService.getCustomerIdsThatPrincipalCanAccess(user).contains(question.getCustomerId())) {
            return ImmutableList.copyOf(weightRepository.findAllByQuestion(question));
        } else {
            return ImmutableList.of();
        }
    }

    public Long getAnswerCount(final Question question) {
        return questionService.getAnswerCount(question);
    }
}
