package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import fi.sangre.renesans.application.dao.GuestDao;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.event.InviteToAfterGameDiscussionEvent;
import fi.sangre.renesans.application.event.InviteToAfterGameEvent;
import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j

@Service
public class AfterGameService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RespondentDao respondentDao;
    private final GuestDao guestDao;
    private final TranslationService translationService;

    public void inviteToAfterGame(@NonNull final SurveyId surveyId
            , @NonNull final Invitation invitation
            , @NonNull final Pair<String, String> replyTo) {

        final Set<IdValueObject<UUID>> toInvite = register(surveyId, invitation);

        applicationEventPublisher.publishEvent(new InviteToAfterGameEvent(
                surveyId,
                invitation.getSubject(),
                invitation.getBody(),
                toInvite,
                replyTo));
    }

    public void inviteToAfterGameDiscussion(@NonNull final SurveyId surveyId
            , @NonNull final QuestionId questionId
            , @NonNull final Invitation invitation
            , @NonNull final Pair<String, String> replyTo) {

        final Set<IdValueObject<UUID>> toInvite = register(surveyId, invitation);

        applicationEventPublisher.publishEvent(new InviteToAfterGameDiscussionEvent(
                surveyId,
                questionId,
                invitation.getSubject(),
                invitation.getBody(),
                toInvite,
                replyTo));
    }

    @NonNull
    private Set<IdValueObject<UUID>> register(@NonNull final SurveyId surveyId,@NonNull final Invitation invitation) {
        final String languageTag = translationService.getLanguageTagOrDefault(invitation.getLanguage());

        final Map<RespondentId, String> existing = respondentDao.findActiveRespondents(surveyId, SurveyRespondent::getEmail);

        final Set<IdValueObject<UUID>> toInvite = new HashSet<>();
        if (invitation.getInviteAll()) {
            toInvite.addAll(existing.keySet());
        }

        final Set<String> invitationEmail = Optional.ofNullable(invitation.getEmails())
                .orElse(ImmutableSet.of());

        toInvite.addAll(existing.entrySet().stream()
                .filter(v -> invitationEmail.contains(v.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet()));

        final Set<String> guestEmails = Sets.difference(invitationEmail, new HashSet<>(existing.values()));

        toInvite.addAll(guestDao.registerGuests(surveyId, guestEmails, languageTag));

        return Collections.unmodifiableSet(toInvite);
    }
}
