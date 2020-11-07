package fi.sangre.renesans.service;

import fi.sangre.renesans.aaa.UserPrincipalService;
import fi.sangre.renesans.dto.*;
import fi.sangre.renesans.graphql.input.InvitationInput;
import fi.sangre.renesans.graphql.input.RecipientInput;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import graphql.GraphQLException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class InvitationService {
    private final RespondentRepository respondentRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final MailService mailService;
    private final UserPrincipalService userPrincipalService;

    @Value("${fi.sangre.renesans.invitation.url}")
    private String url;

    @Value("${fi.sangre.renesans.survey.url}")
    private String surveyUrl;
    private static final String RESPONDENT_GROUP_ID_PARAM = "groupId";
    private static final String HASH_PARAM = "hash";

    @Autowired
    public InvitationService(
            RespondentRepository respondentRepository,
            RespondentGroupRepository respondentGroupRepository,
            MailService mailService,
            UserPrincipalService userPrincipalService
    ) {
        this.respondentRepository = respondentRepository;
        this.respondentGroupRepository = respondentGroupRepository;
        this.mailService = mailService;
        this.userPrincipalService = userPrincipalService;
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
        Map<String, Respondent> respondentsByEmail = respondents.stream().collect(Collectors.toMap(Respondent::getEmail, respondent -> respondent));

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

    public InvitationDetailsDto getInvitation(String hash) {
        // TODO implement
        return null;
    }

    public InvitationDetailsDto moveInvitationToGroup(String invitationHash, String newGroupId) {
        InvitationUpdateDto invitation = new InvitationUpdateDto();
        invitation.setGroupId(newGroupId);

        RestTemplate restTemplate = new RestTemplate();
        String updateInvitationByHashURL = url + "hash/" + invitationHash;

        return restTemplate.postForObject(updateInvitationByHashURL, invitation, InvitationDetailsDto.class);
    }
}
