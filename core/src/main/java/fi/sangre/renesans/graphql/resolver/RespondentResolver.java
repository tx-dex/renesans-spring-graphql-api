package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.graphql.Context;
import fi.sangre.renesans.model.Answer;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.RespondentGroupService;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
@Transactional
@Deprecated
public class RespondentResolver implements GraphQLResolver<Respondent> {
    @Autowired
    private AnswerService answerService;

    @Autowired
    private RespondentGroupService respondentGroupService;

    public List<Answer> getAnswers(Respondent respondent) {
        return ImmutableList.of();
    }

    public Date answerTime(Respondent r) {
        return null;
    }

    public String getOfficeLocation(Respondent respondent, DataFetchingEnvironment environment) {
        if (respondent.getCountry() == null) {
            return null;
        }
        Context context = environment.getContext();
        String languageCode = context.getLanguageCode();

        String code = languageCode != null ? languageCode : "en";

        Locale locale = new Locale(code, respondent.getCountry());

        return locale.getDisplayCountry(Locale.forLanguageTag(code));
    }

    public RespondentGroup getRespondentGroup(Respondent respondent) {
        return respondentGroupService.getGroupForRespondent(respondent);
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Respondent.State getState(Respondent respondent) {
        return respondent.getState();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Date getModified(Respondent respondent) {
        return respondent.getModified();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Date getCreated(Respondent respondent) {
        return respondent.getCreated();
    }

    // do not expose internals to the public
    @PreAuthorize("isAuthenticated()")
    public Boolean getConsent(Respondent respondent) {
        return respondent.getConsent();
    }

}
