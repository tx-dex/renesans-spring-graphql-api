package fi.sangre.renesans.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
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
import fi.sangre.renesans.dto.*;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.graphql.input.InvitationInput;
import fi.sangre.renesans.graphql.input.RecipientInput;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import graphql.GraphQLException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
    private final DefaultMustacheFactory mustacheFactory;

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
                .tags(ImmutableMap.of("type", "invitation",
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

        final Mustache body = mustacheFactory.compile(new StringReader(bodyTemplate), "body-template");

        final StringWriter writer = new StringWriter();
        body.execute(writer, ImmutableMap.of("invitation_link", invitationLink));

        return writer.toString();
    }

    private String getSurveyLink(Respondent respondent, String languageCode) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(surveyUrl);
        if (languageCode != null) {
            builder.pathSegment(languageCode);
        }
        builder.pathSegment("u", respondent.getId());

        return builder.toUriString();
    }

    private Map<String, Object> recipientParameters(Respondent respondent) {
        Map<String, Object> parameters = new HashMap<>();

        // add individual survey link om English
        parameters.put("surveyLink", getSurveyLink(respondent, null));
        parameters.put("surveyLinkEN", getSurveyLink(respondent, "en"));

        // add individual survey link in Chinese
        parameters.put("surveyLinkZH", getSurveyLink(respondent, "zh"));

        // add individual survey link in Finnish
        parameters.put("surveyLinkFI", getSurveyLink(respondent, "fi"));

        return parameters;
    }

    public ResultDetailsDto sendInvitation(InvitationInput invitationInput, List<RecipientInput> recipients) {

        InvitationDto invitation = new InvitationDto();
        invitation.setSubject(invitationInput.getSubject());
        invitation.setBody(mailService.wrapBody(invitationInput.getBody()));

        final User user = userPrincipalService.getLoggedInUser();
        if (user != null) {
            final String senderName = String.format("%s %s | weCan5", user.getFirstName(), user.getLastName());
            invitation.setSenderName(senderName);
            invitation.setReplyTo(new EmailNamePair(user.getEmail(), senderName));
        }

        // generate list of respondent objects in state INVITED and with email set to invitation email
        List<Respondent> respondents = recipients.stream()
                .map(r -> Respondent.builder()
                        .email(r.getEmail())
                        .respondentGroup(respondentGroupRepository.findById(r.getRespondentGroupId()).orElseGet(null))
                        .state(Respondent.State.INVITED)
                        .build()
                )
                .collect(Collectors.toList());

        // store created respondents
        respondentRepository.saveAll(respondents);

        // map respondents to an array by email for easier access later on
        Map<String, Respondent> respondentsByEmail = respondents.stream().collect(toMap(Respondent::getEmail, respondent -> respondent));

        // generate invitation recipient data objects
        invitation.setRecipients(recipients.stream()
                .map(r -> RecipientDto.builder()
                        .groupId(r.getRespondentGroupId())
                        .email(r.getEmail())
                        .parameters(recipientParameters(respondentsByEmail.get(r.getEmail())))
                        .build()
                )
                .collect(Collectors.toList())
        );

        // send invitation details to mailjet api
        RestTemplate restTemplate = new RestTemplate();
        ResultDetailsDto results = restTemplate.postForObject(url, invitation, ResultDetailsDto.class);

        // from results, iterate recipients and add returned invitation hash to the object
        results.getRecipients().forEach(r -> respondentsByEmail.get(r.getEmail()).setInvitationHash(r.getHash()));

        // store new respondents with the hashes
        respondentRepository.saveAll(respondentsByEmail.values());

        return results;
    }

    public List<InvitationDetailsDto> getInvitationsByRespondentGroup(String respondentGroupId) {

        RestTemplate restTemplate = new RestTemplate();
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url);
        builder.queryParam(RESPONDENT_GROUP_ID_PARAM, respondentGroupId);
        try {
            ResponseEntity<InvitationDetailsDto[]> response = restTemplate
                    .getForEntity(builder.toUriString(), InvitationDetailsDto[].class);

            List<InvitationDetailsDto> invitationDetails = Arrays.asList(response.getBody());

            return invitationDetails.stream().peek(d -> {
                Respondent r = respondentRepository.findByInvitationHash(d.getHash());
                if (r != null) {
                    Respondent.State state = r.getState();
                    if (state != Respondent.State.INVITED) {
                        d.setStatus(r.getState().toString().toLowerCase());
                        if (state == Respondent.State.FINISHED && !r.getEmail().equals(d.getEmail())) {
                            d.setEmail(d.getEmail() + " (" + r.getEmail() + ")");
                        }
                    }
                }
            }).collect(Collectors.toList());

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return new ArrayList<>();
            }
            throw new GraphQLException(e);
        } catch (Exception e) {
            throw new GraphQLException(e);
        }
    }

    public List<InvitationDetailsDto> getInvitationsByEmail(String respondentGroupId, String email, String status) {

        RestTemplate restTemplate = new RestTemplate();
        String invitationsByEmailURL = url + "email/" + email;
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(invitationsByEmailURL);
        builder.queryParam(RESPONDENT_GROUP_ID_PARAM, respondentGroupId);

        if (!StringUtils.isEmpty(status)) {
            builder.queryParam("status", status);
        }

        try {
            ResponseEntity<InvitationDetailsDto[]> response = restTemplate
                    .getForEntity(builder.toUriString(), InvitationDetailsDto[].class);
            return Arrays.asList(response.getBody());
        } catch (Exception e) {
            throw new GraphQLException(e);
        }
    }

}
