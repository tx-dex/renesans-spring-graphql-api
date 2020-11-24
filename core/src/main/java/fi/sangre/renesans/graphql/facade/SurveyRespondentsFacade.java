package fi.sangre.renesans.graphql.facade;

import fi.sangre.renesans.application.model.SurveyRespondent;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.graphql.input.FilterInput;
import fi.sangre.renesans.graphql.input.RespondentInvitationInput;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyRespondentsFacade {
    private final OrganizationSurveyService organizationSurveyService;

    @NonNull
    public Collection<SurveyRespondent> getSurveyRespondents(@NonNull final UUID surveyId,
                                                             @Nullable final List<FilterInput> filters,
                                                             @NonNull final String languageCode) {

        // TODO: combine with mail Service data
        return organizationSurveyService.findRespondents(surveyId);
    }

    @NonNull
    public Collection<SurveyRespondent> inviteRespondents(@NonNull final UUID surveyId,
                                                          @NonNull final List<RespondentInvitationInput> invitations,
                                                          @Nullable final List<FilterInput> filters,
                                                          @NonNull final String languageCode) {
        organizationSurveyService.inviteRespondents(surveyId, invitations.stream()
                .map(e -> Invitation.builder()
                        .email(e.getEmail())
                        .build())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet)));

        return getSurveyRespondents(surveyId, filters, languageCode);
    }
}