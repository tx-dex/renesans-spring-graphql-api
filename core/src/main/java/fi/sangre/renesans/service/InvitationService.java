package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import com.sangre.mail.dto.MailInfoDto;
import com.sangre.mail.dto.MailStatus;
import fi.sangre.renesans.application.client.FeignMailClient;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.event.InviteToAfterGameDiscussionEvent;
import fi.sangre.renesans.application.event.InviteToAfterGameEvent;
import fi.sangre.renesans.application.event.InviteToQuestionnaireEvent;
import fi.sangre.renesans.application.model.IdValueObject;
import fi.sangre.renesans.application.model.RespondentEmail;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Future;

import static fi.sangre.renesans.config.ApplicationConfig.ASYNC_EXECUTOR_NAME;
import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Service
public class InvitationService {
    private static final String MAIL_TYPE_TAG = "email-type";
    private static final String MAIL_TYPE_QUESTIONNAIRE_INVITATION_VALUE = "survey-invitation";
    private static final String MAIL_TYPE_AFTER_GAME_INVITATION_VALUE = "after-game-invitation";
    private static final String MAIL_TYPE_AFTER_GAME_DISCUSSION_INVITATION_VALUE = "discussion-invitation";
    private static final String SURVEY_ID_TAG = "survey-id";
    private static final String RESPONDENT_ID_TAG = "respondent-id";
    private static final String DISCUSSION_ID_TAG = "discussion-id";
    private static final String INVITATION_PATH = "invitation";

    private final SurveyRespondentRepository surveyRespondentRepository;
    private final MailService mailService;
    private final FeignMailClient feignMailClient;
    private final RespondentDao respondentDao;
    private final TemplateService templateService;

    @Value("${fi.sangre.renesans.survey.url}")
    private String surveyUrl;

    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<RespondentEmail, MailStatus>> getInvitationStatuses(final SurveyId surveyId) {
        log.debug("Getting emails statuses list");
        return new AsyncResult<>(feignMailClient.findLatestEmails(ImmutableMap.of(SURVEY_ID_TAG, surveyId.asString()
                , MAIL_TYPE_TAG, MAIL_TYPE_QUESTIONNAIRE_INVITATION_VALUE
                )).stream()
                .collect(collectingAndThen(toMap(e -> new RespondentEmail(e.getRecipient()), MailInfoDto::getStatus), Collections::unmodifiableMap)));
    }

    @Async(ASYNC_EXECUTOR_NAME)
    @TransactionalEventListener
    public void handle(@NonNull final InviteToQuestionnaireEvent event) {
        log.debug("Handling invitation event: {}", event);

        event.getRespondentIds().forEach(respondentId -> {
            try {
                log.info("Sending invitation to respondent(id={})", respondentId);
                sendInvitationEmail(respondentId, event.getSubject(), event.getBody(), event.getReplyTo());
            } catch (final Exception ex) {
                log.warn("Cannot invite to questionnaire '{}'", respondentId, ex);
                setErrorSilently(respondentId, ex.getMessage());
            }
        });
    }

    @Async(ASYNC_EXECUTOR_NAME)
    @EventListener
    public void handle(@NonNull final InviteToAfterGameEvent event) {
        log.debug("Handling invitation event: {}", event);

        event.getRespondentIds().forEach(respondentId -> {
            try {
                log.info("Sending invitation to respondent(id={})", respondentId);
                sendAfterGameInvitation(respondentId, event.getSubject(), event.getBody(), event.getReplyTo());
            } catch (final Exception ex) {
                log.warn("Cannot invite to after game '{}'", respondentId, ex);
                setErrorSilently(respondentId, ex.getMessage());
            }
        });
    }

    @Async(ASYNC_EXECUTOR_NAME)
    @EventListener
    public void handle(@NonNull final InviteToAfterGameDiscussionEvent event) {
        log.debug("Handling invitation event: {}", event);

        event.getRespondentIds().forEach(respondentId -> {
            try {
                log.info("Sending invitation to respondent(id={})", respondentId);
                sendAfterGameDiscussionInvitation(respondentId, event.getQuestionId(), event.getSubject(), event.getBody(), event.getReplyTo());
            } catch (final Exception ex) {
                log.warn("Cannot invite to after game discussion '{}'", respondentId, ex);
                setErrorSilently(respondentId, ex.getMessage());
            }
        });
    }

    private void sendInvitationEmail(@NonNull final RespondentId respondentId,
                                     @NonNull final String subject,
                                     @NonNull final String body,
                                     @NonNull final Pair<String, String> replyTo) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new InternalServiceException("Cannot get respondent"));
        final SurveyId surveyId = new SurveyId(respondent.getSurveyId());
        final String invitationLink = getQuestionnaireInvitationLink(respondentId, respondent.getInvitationHash());

        mailService.sendEmail(respondent.getEmail(),
                subject,
                composeBody(invitationLink, body),
                ImmutableMap.of(MAIL_TYPE_TAG, MAIL_TYPE_QUESTIONNAIRE_INVITATION_VALUE,
                        SURVEY_ID_TAG, surveyId.asString(),
                        RESPONDENT_ID_TAG, respondentId.asString()),
                replyTo);
    }

    private void sendAfterGameInvitation(@NonNull final IdValueObject<? extends UUID> id,
                                         @NonNull final String subject,
                                         @NonNull final String body,
                                         @NonNull final Pair<String, String> replyTo) {
        if (id instanceof RespondentId) {
            final RespondentId respondentId = (RespondentId) id;
            final SurveyRespondent respondent = getRespondent(respondentId);
            final SurveyId surveyId = new SurveyId(respondent.getSurveyId());
            final String invitationLink = getAfterGameInvitationLink(respondentId, respondent.getInvitationHash());

            mailService.sendEmail(respondent.getEmail(),
                    subject,
                    composeBody(invitationLink, body),
                    ImmutableMap.of(
                            MAIL_TYPE_TAG, MAIL_TYPE_AFTER_GAME_INVITATION_VALUE
                            , SURVEY_ID_TAG, surveyId.asString()
                            , RESPONDENT_ID_TAG, respondentId.asString()
                    ), replyTo);
        }
    }

    private void sendAfterGameDiscussionInvitation(@NonNull final IdValueObject<? extends UUID> id,
                                                   @NonNull final QuestionId questionId,
                                                   @NonNull final String subject,
                                                   @NonNull final String body,
                                                   @NonNull final Pair<String, String> replyTo) {
        if (id instanceof RespondentId) {
            final RespondentId respondentId = (RespondentId) id;
            final SurveyRespondent respondent = getRespondent(respondentId);
            final SurveyId surveyId = new SurveyId(respondent.getSurveyId());
            final String invitationLink = getAfterGameDiscussionInvitationLink(respondentId, respondent.getInvitationHash(), questionId);

            mailService.sendEmail(respondent.getEmail(),
                    subject,
                    composeBody(invitationLink, body),
                    ImmutableMap.of(
                            MAIL_TYPE_TAG, MAIL_TYPE_AFTER_GAME_DISCUSSION_INVITATION_VALUE
                            , SURVEY_ID_TAG, surveyId.asString()
                            , RESPONDENT_ID_TAG, respondentId.asString()
                            , DISCUSSION_ID_TAG, questionId.asString()
                    ), replyTo);
        }
    }

    @NonNull
    private SurveyRespondent getRespondent(@NonNull final RespondentId respondentId) {
        return surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new InternalServiceException("Cannot get respondent"));
    }

    @NonNull
    private String getQuestionnaireInvitationLink(@NonNull final IdValueObject<?> id, @NonNull final String invitationHash) {
        return UriComponentsBuilder.fromUriString(surveyUrl)
                .pathSegment(id.asString(),
                        INVITATION_PATH,
                        invitationHash)
                .toUriString();
    }

    @NonNull
    private String getAfterGameInvitationLink(@NonNull final IdValueObject<?> id, @NonNull final String invitationHash) {
        return UriComponentsBuilder.fromUriString(surveyUrl)
                .pathSegment(id.asString(),
                        INVITATION_PATH,
                        invitationHash,
                        "after-game")
                .toUriString();
    }

    @NonNull
    private String getAfterGameDiscussionInvitationLink(@NonNull final IdValueObject<?> id, @NonNull final String invitationHash, @NonNull final QuestionId discussionId) {
        return UriComponentsBuilder.fromUriString(surveyUrl)
                .pathSegment(id.asString(),
                        INVITATION_PATH,
                        invitationHash,
                        "after-game",
                        discussionId.asString())
                .toUriString();
    }

    @NonNull
    private String composeBody(@NonNull final String invitationLink, @NonNull final String bodyTemplate) {
        return templateService.templateBody(bodyTemplate, ImmutableMap.of("invitation_link", invitationLink));
    }

    private void setErrorSilently(@NonNull final IdValueObject<? extends UUID> id, @NonNull final String error) {
        try {
            if (id instanceof RespondentId) {
                final RespondentId respondentId = (RespondentId) id;
                respondentDao.updateRespondentError(respondentId, error);
            }
        } catch (final Exception ex) {
            log.warn("Cannot update status on error '{}'", id, ex);
        }
    }
}
