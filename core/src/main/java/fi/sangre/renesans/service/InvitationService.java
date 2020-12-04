package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import com.sangre.mail.dto.MailDto;
import com.sangre.mail.dto.MailInfoDto;
import com.sangre.mail.dto.MailStatus;
import com.sangre.mail.dto.NameEmailPairDto;
import fi.sangre.renesans.aaa.UserPrincipalService;
import fi.sangre.renesans.application.client.FeignMailClient;
import fi.sangre.renesans.application.event.InviteRespondentsEvent;
import fi.sangre.renesans.application.model.RespondentEmail;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Future;

import static fi.sangre.renesans.config.ApplicationConfig.ASYNC_EXECUTOR_NAME;
import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Service
public class InvitationService {
    private final RespondentRepository respondentRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final MailService mailService;
    private final UserPrincipalService userPrincipalService;
    private final FeignMailClient feignMailClient;

    @Value("${fi.sangre.renesans.invitation.url}")
    private String url;

    @Value("${fi.sangre.renesans.survey.url}")
    private String surveyUrl;
    private static final String RESPONDENT_GROUP_ID_PARAM = "groupId";

    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<RespondentEmail, MailStatus>> getInvitationStatuses(final SurveyId surveyId) {
        log.debug("Getting emails statuses list");
        return new AsyncResult<>(feignMailClient.findLatestEmails(ImmutableMap.of("survey-id", surveyId.getValue().toString()))
                .stream()
                .collect(collectingAndThen(toMap(e -> new RespondentEmail(e.getRecipient()), MailInfoDto::getStatus), Collections::unmodifiableMap)));
    }

    @Async(ASYNC_EXECUTOR_NAME)
    @TransactionalEventListener
    public void handle(@NonNull final InviteRespondentsEvent event) {
        log.debug("Handling invitation event: {}", event);

        event.getRespondentIds().forEach(respondentId -> {
            try {
                sendInvitationEmail(respondentId, event.getSubject(), event.getBody(), event.getReplyTo());
                log.info("Sending invitation to respondent(id={})", respondentId);
            } catch (final Exception ex) {
                log.warn("Cannot invite respondent(id={})", respondentId, ex);
                //TODO: mark as an error in respondent table
            }
        });
    }

    private void sendInvitationEmail(@NonNull final RespondentId respondentId,
                                     @NonNull final String subject,
                                     @NonNull final String body,
                                     @NonNull final Pair<String, String> replyTo) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new InternalServiceException("Cannot get respondent"));

        final MailDto message = MailDto.builder()
                .recipient(respondent.getEmail())
                .subject(subject)
                .body(composeMail(respondent, body))
                .replyTo(NameEmailPairDto.builder()
                        .name(replyTo.getLeft())
                        .email(replyTo.getRight())
                        .build())
                .tags(ImmutableMap.of("email-type", "survey-invitation",
                        "survey-id", respondent.getSurveyId().toString(),
                        "respondent-id", respondentId.getValue().toString()))
                .build();
        feignMailClient.sendEmail(message);
    }

    @NonNull
    private String composeMail(@NonNull final SurveyRespondent respondent, @NonNull final String bodyTemplate) {
        final String invitationLink = UriComponentsBuilder.fromUriString(surveyUrl)
                .pathSegment(respondent.getId().toString(), "invitation", respondent.getInvitationHash())
                .toUriString();

        return mailService.templateBody(bodyTemplate, ImmutableMap.of("invitation_link", invitationLink));
    }
}
