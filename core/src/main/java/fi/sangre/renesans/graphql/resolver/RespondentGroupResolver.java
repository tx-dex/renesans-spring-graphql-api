package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.repository.QuestionGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import fi.sangre.renesans.service.CustomerService;
import fi.sangre.renesans.service.QuestionService;
import fi.sangre.renesans.service.SurveyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static fi.sangre.renesans.graphql.output.CatalystProxy.toProxies;

@RequiredArgsConstructor

@Component
@Transactional
@Deprecated
public class RespondentGroupResolver implements GraphQLResolver<RespondentGroup> {
    private final RespondentRepository respondentRepository;
    private final QuestionGroupRepository questionGroupRepository;
    private final SurveyService surveyService;
    private final CustomerService customerService;
    private final QuestionService questionService;

    @PreAuthorize("isAuthenticated()")
    public List<Respondent> getRespondents(RespondentGroup respondentGroup) {
        return respondentRepository.findByRespondentGroupAndState(respondentGroup, Respondent.State.FINISHED);
    }

    @PreAuthorize("isAuthenticated()")
    public Survey getSurvey(RespondentGroup respondentGroup) {
        return surveyService.getSurvey(respondentGroup);
    }

    @PreAuthorize("isAuthenticated()")
    public Customer getCustomer(RespondentGroup respondentGroup) {
        return customerService.getCustomer(respondentGroup);
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public String getTitle(RespondentGroup respondentGroup) {
        return respondentGroup.getTitle();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public String getDescription(RespondentGroup respondentGroup) {
        return respondentGroup.getDescription();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Boolean getIsDefault(RespondentGroup respondentGroup) {
        return respondentGroup.getIsDefault();
    }

    @Deprecated
    public List<QuestionGroup> getQuestionGroups(RespondentGroup respondentGroup) {
        return questionGroupRepository.findByRespondentGroupsContaining(respondentGroup);
    }

    public List<CatalystProxy> getCatalysts(final RespondentGroup respondentGroup) {
        return toProxies(questionService.getCatalysts(respondentGroup));
    }

    public Long getRespondentCount(RespondentGroup respondentGroup) {
        return respondentRepository.countByRespondentGroup(respondentGroup);
    }
}
