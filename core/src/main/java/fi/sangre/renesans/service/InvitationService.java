package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import com.sangre.mail.dto.MailInfoDto;
import com.sangre.mail.dto.MailStatus;
import com.sangre.mail.dto.attachements.Base64AttachmentDto;
import fi.sangre.renesans.application.client.FeignMailClient;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.event.InviteToAfterGameDiscussionEvent;
import fi.sangre.renesans.application.event.InviteToAfterGameEvent;
import fi.sangre.renesans.application.event.InviteToQuestionnaireEvent;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.persistence.model.SurveyGuest;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.repository.SurveyGuestRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
    private static final Base64AttachmentDto LOGO = new Base64AttachmentDto("logo", "image/png", "logo.png", resourceToString("/templates/email-logo.base64"));
    private static final String HTML_BASE_TEMPLATE = resourceToString("/templates/email-template.html");

    private static final String MAIL_TEMPLATE_TEXT_GROUP = "email_template";
    private static final String HEADING_TITLE_KEY = "heading_title";
    private static final String HEADING_PARAGRAPH_KEY = "heading_paragraph";
    private static final String INVITATION_BUTTON_KEY = "invitation_button";


    private static final String MAIL_TYPE_TAG = "email-type";
    private static final String MAIL_TYPE_QUESTIONNAIRE_INVITATION_VALUE = "survey-invitation";
    private static final String MAIL_TYPE_AFTER_GAME_INVITATION_VALUE = "after-game-invitation";
    private static final String MAIL_TYPE_AFTER_GAME_DISCUSSION_INVITATION_VALUE = "discussion-invitation";
    private static final String SURVEY_ID_TAG = "survey-id";
    private static final String RESPONDENT_ID_TAG = "respondent-id";
    private static final String GUEST_ID_TAG = "guest-id";
    private static final String DISCUSSION_ID_TAG = "discussion-id";
    private static final String INVITATION_PATH = "invitation";

    private final SurveyRespondentRepository surveyRespondentRepository;
    private final SurveyGuestRepository surveyGuestRepository;
    private final MailService mailService;
    private final FeignMailClient feignMailClient;
    private final RespondentDao respondentDao;
    private final SurveyDao surveyDao;
    private final TemplateService templateService;
    private final TranslationService translationService;
    private final MultilingualUtils multilingualUtils;

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
        final String languageTag = getLanguageTag(respondent);
        final SurveyId surveyId = new SurveyId(respondent.getSurveyId());
        final String invitationLink = getQuestionnaireInvitationLink(respondentId, respondent.getInvitationHash());

        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);
        //TODO: get language tag from "respondent"

        mailService.sendEmail(respondent.getEmail(),
                subject,
                composeHtmlBody(invitationLink, body, survey, languageTag),
                null,
                ImmutableMap.of(MAIL_TYPE_TAG, MAIL_TYPE_QUESTIONNAIRE_INVITATION_VALUE,
                        SURVEY_ID_TAG, surveyId.asString(),
                        RESPONDENT_ID_TAG, respondentId.asString()),
                replyTo,
                LOGO);
    }

    private void sendAfterGameInvitation(@NonNull final IdValueObject<? extends UUID> id,
                                         @NonNull final String subject,
                                         @NonNull final String body,
                                         @NonNull final Pair<String, String> replyTo) {
        final SurveyId surveyId;
        final String languageTag;
        final String email;
        final String invitationLink;
        final Map<String, String> tags;

        if (id instanceof RespondentId) {
            final RespondentId respondentId = (RespondentId) id;
            final SurveyRespondent respondent = getRespondent(respondentId);
            languageTag = getLanguageTag(respondent);
            email = respondent.getEmail();
            surveyId = new SurveyId(respondent.getSurveyId());
            invitationLink = getAfterGameInvitationLink(respondentId, respondent.getInvitationHash());
            tags = ImmutableMap.of(
                    MAIL_TYPE_TAG, MAIL_TYPE_AFTER_GAME_INVITATION_VALUE
                    , SURVEY_ID_TAG, surveyId.asString()
                    , RESPONDENT_ID_TAG, id.asString()
            );
        } else if (id instanceof GuestId) {
            final GuestId guestId = (GuestId) id;
            final SurveyGuest guest = getGuest(guestId);
            languageTag = getLanguageTag(guest);
            email = guest.getEmail();
            surveyId = new SurveyId(guest.getSurveyId());
            invitationLink = getAfterGameInvitationLink(guestId, guest.getInvitationHash());
            tags = ImmutableMap.of(
                    MAIL_TYPE_TAG, MAIL_TYPE_AFTER_GAME_INVITATION_VALUE
                    , SURVEY_ID_TAG, surveyId.asString()
                    , GUEST_ID_TAG, id.asString()
            );
        } else {
            throw new RuntimeException("Invalid id");
        }

        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);

        mailService.sendEmail(email,
                subject,
                composeHtmlBody(invitationLink, body, survey, languageTag),
                null,
                tags,
                replyTo,
                LOGO);
    }

    private void sendAfterGameDiscussionInvitation(@NonNull final IdValueObject<? extends UUID> id,
                                                   @NonNull final QuestionId questionId,
                                                   @NonNull final String subject,
                                                   @NonNull final String body,
                                                   @NonNull final Pair<String, String> replyTo) {
        final SurveyId surveyId;
        final String email;
        final String invitationLink;
        final Map<String, String> tags;
        final String languageTag;

        if (id instanceof RespondentId) {
            final RespondentId respondentId = (RespondentId) id;
            final SurveyRespondent respondent = getRespondent(respondentId);
            languageTag = getLanguageTag(respondent);
            email = respondent.getEmail();
            surveyId = new SurveyId(respondent.getSurveyId());
            invitationLink = getAfterGameDiscussionInvitationLink(respondentId, respondent.getInvitationHash(), questionId);
            tags = ImmutableMap.of(
                    MAIL_TYPE_TAG, MAIL_TYPE_AFTER_GAME_DISCUSSION_INVITATION_VALUE
                    , SURVEY_ID_TAG, surveyId.asString()
                    , RESPONDENT_ID_TAG, respondentId.asString()
                    , DISCUSSION_ID_TAG, questionId.asString()
            );
        } else if (id instanceof GuestId) {
            final GuestId guestId = (GuestId) id;
            final SurveyGuest guest = getGuest(guestId);
            languageTag = getLanguageTag(guest);
            email = guest.getEmail();
            surveyId = new SurveyId(guest.getSurveyId());
            invitationLink = getAfterGameInvitationLink(guestId, guest.getInvitationHash());
            tags = ImmutableMap.of(
                    MAIL_TYPE_TAG, MAIL_TYPE_AFTER_GAME_DISCUSSION_INVITATION_VALUE
                    , SURVEY_ID_TAG, surveyId.asString()
                    , GUEST_ID_TAG, id.asString()
                    , DISCUSSION_ID_TAG, questionId.asString()
            );
        } else {
            throw new RuntimeException("Invalid id");
        }

        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);

        mailService.sendEmail(email,
                subject,
                composeHtmlBody(invitationLink, body, survey, languageTag),
                null,
                tags,
                replyTo,
                LOGO);
    }

    @NonNull
    private String getLanguageTag(@NonNull final SurveyRespondent respondent) {
        return respondent.getInvitationLanguageTag(); //TODO: use selected language tag if language was changed
    }

    @NonNull
    private String getLanguageTag(@NonNull final SurveyGuest guest) {
        return guest.getInvitationLanguageTag(); //TODO: use selected language tag if language was changed
    }

    @NonNull
    private SurveyRespondent getRespondent(@NonNull final RespondentId respondentId) {
        return surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new InternalServiceException("Cannot get respondent"));
    }

    @NonNull
    private SurveyGuest getGuest(@NonNull final GuestId guestId) {
        return surveyGuestRepository.findById(guestId.getValue())
                .orElseThrow(() -> new InternalServiceException("Cannot get guest"));
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
    private String composeHtmlBody(@NonNull final String invitationLink,
                                   @NonNull final String bodyText,
                                   @NonNull final OrganizationSurvey survey,
                                   @NonNull final String languageTag) {
        final Map<String, MultilingualText> texts = Optional.ofNullable(survey.getStaticTexts())
                .map(v -> v.get(MAIL_TEMPLATE_TEXT_GROUP))
                .map(StaticTextGroup::getTexts)
                .orElse(StaticTextGroup.EMPTY);
        final MultilingualText defaultHeadingTitle = translationService.getPhrases(MAIL_TEMPLATE_TEXT_GROUP, HEADING_TITLE_KEY);
        final MultilingualText defaultHeadingParagraph = translationService.getPhrases(MAIL_TEMPLATE_TEXT_GROUP, HEADING_PARAGRAPH_KEY);
        final MultilingualText defaultInvitationButton = translationService.getPhrases(MAIL_TEMPLATE_TEXT_GROUP, INVITATION_BUTTON_KEY);
        final MultilingualText headingTitle = texts.getOrDefault(HEADING_TITLE_KEY, multilingualUtils.empty());
        final MultilingualText headingParagraph = texts.getOrDefault(HEADING_PARAGRAPH_KEY, multilingualUtils.empty());
        final MultilingualText invitationButton = texts.getOrDefault(INVITATION_BUTTON_KEY, multilingualUtils.empty());

        return templateService.templateBody(HTML_BASE_TEMPLATE, ImmutableMap.of(
                HEADING_TITLE_KEY, multilingualUtils.combine(defaultHeadingTitle, headingTitle).getPhrase(languageTag),
                HEADING_PARAGRAPH_KEY, multilingualUtils.combine(defaultHeadingParagraph, headingParagraph).getPhrase(languageTag),
                INVITATION_BUTTON_KEY, multilingualUtils.combine(defaultInvitationButton, invitationButton).getPhrase(languageTag),
                "invitation_link", invitationLink,
                "email_content", bodyText));
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

    private static String resourceToString(@NonNull final String resource) {
        try {
            return IOUtils.resourceToString(resource, StandardCharsets.UTF_8);
        } catch (final Exception ex) {
            log.warn("cannot load resource", ex);
            throw new RuntimeException("Cannot load resource", ex);
        }
    }
}
