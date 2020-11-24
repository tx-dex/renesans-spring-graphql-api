package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.application.model.SurveyRespondent;
import fi.sangre.renesans.graphql.input.FilterInput;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyRespondentsFacade {
    private final OrganizationSurveyService organizationSurveyService;

    @NonNull
    public Collection<SurveyRespondent> getSurveyRespondents(@NonNull final UUID surveyId,
                                                             @Nullable final List<FilterInput> filters,
                                                             @NonNull final String languageCode) {

        return organizationSurveyService.findRespondents(surveyId);
    }
}