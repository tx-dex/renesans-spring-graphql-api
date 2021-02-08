package fi.sangre.renesans.service;

import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.event.InviteToAfterGameDiscussionEvent;
import fi.sangre.renesans.application.event.InviteToAfterGameEvent;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Slf4j

@Service
public class AfterGameService {
    private final ApplicationEventPublisher applicationEventPublisher;
    private final RespondentDao respondentDao;

    public void inviteToAfterGame(@NonNull final SurveyId surveyId
            , @NonNull final Invitation invitation
            , @NonNull final Pair<String, String> replyTo) {

        final Set<RespondentId> existing;
        if (invitation.getInviteAll()) {
            existing = respondentDao.findRespondents(surveyId);
        } else {
            existing = respondentDao.findRespondentsByEmails(surveyId, invitation.getEmails());
        }

        applicationEventPublisher.publishEvent(new InviteToAfterGameEvent(
                surveyId,
                invitation.getSubject(),
                invitation.getBody(),
                existing,
                replyTo));
    }

    public void inviteToAfterGameDiscussion(@NonNull final SurveyId surveyId
            , @NonNull final QuestionId questionId
            , @NonNull final Invitation invitation
            , @NonNull final Pair<String, String> replyTo) {

        final Set<RespondentId> existing;
        if (invitation.getInviteAll()) {
            existing = respondentDao.findRespondents(surveyId);
        } else {
            existing = respondentDao.findRespondentsByEmails(surveyId, invitation.getEmails());
        }

        applicationEventPublisher.publishEvent(new InviteToAfterGameDiscussionEvent(
                surveyId,
                questionId,
                invitation.getSubject(),
                invitation.getBody(),
                existing,
                replyTo));
    }
}
