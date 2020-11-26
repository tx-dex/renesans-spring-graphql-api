package fi.sangre.renesans.service;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.dto.InvitationDto;
import fi.sangre.renesans.dto.RecipientDto;
import fi.sangre.renesans.dto.ResultDetailsDto;
import fi.sangre.renesans.model.User;
import graphql.GraphQLException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

@Slf4j
@Service
public class MailService {
    private static final MediaType MEDIA_TYPE_HTML = MediaType.parse("text/html; charset=utf-8");
    private static final String RESET_PASSWORD_GROUP_ID = "wecan-reset-user-password";
    private static final String RESET_PASSWORD_URI_PATH = "admin/reset";
    private static final String RESET_PASSWORD_MAIL_SUBJECT_MULTILINGUAL_KEY = "user_reset_password_mail_subject";
    private static final String RESET_PASSWORD_MAIL_BODY_MULTILINGUAL_KEY = "user_reset_password_mail_body";
    private static final String DEFAULT_RESET_PASSWORD_MAIL_SUBJECT = "Password reset for WeCan";
    private static final String DEFAULT_RESET_PASSWORD_MAIL_BODY = "<h2>Greetings from weCan!</h2><p>Someone has requested a link to change your password. You can do this through this link:</p><p><a href=\"{{ reset_link }}\">Reset my password</a></p><p>The link will be valid for {{ reset_link_expiration_time }}. If you didn't request this, please ignore this email.</p><p>Best,<br/>weCan<br/>www.wecan5.com</p>";
    private static final String ACTIVATION_MAIL_SUBJECT_MULTILINGUAL_KEY = "user_activation_mail_subject";
    private static final String ACTIVATION_MAIL_BODY_MULTILINGUAL_KEY = "user_activation_mail_body";
    private static final String DEFAULT_ACTIVATION_MAIL_SUBJECT = "Account activation for WeCan";
    private static final String DEFAULT_ACTIVATION_MAIL_BODY = "<h2>Greetings from weCan!</h2><p>A weCan Key User account has been created for you with username: {{ username }}. To start using the account you need to set up a password. You can do that here:</p><p><a href=\"{{ reset_link }}\">Activate account</a></p><p>The link will be valid for {{ reset_link_expiration_time }}.</p><p>Best,<br/>weCan<br/>www.wecan5.com</p>";
    private final TokenService tokenService;
    private final MultilingualService multilingualService;
    private final OkHttpClient client;

    @Value("${fi.sangre.renesans.admin.url}")
    private String adminAppUrl;

    @Value("${fi.sangre.renesans.invitation.url}")
    private String url;

    @Value("${fi.sangre.renesans.emailTemplates.url}")
    private String templateService;

    @Value("${fi.sangre.renesans.email.useTemplate}")
    private Boolean useTemplate = false;

    @Autowired
    public MailService(
            final TokenService tokenService,
            final MultilingualService multilingualService
    ) {
        checkArgument(tokenService != null, "TokenService is required");
        checkArgument(multilingualService != null, "MultilingualPhraseService is required");

        this.tokenService = tokenService;
        this.multilingualService = multilingualService;
        this.client = new OkHttpClient();
    }

    public String wrapBody(final String body) {
        if (useTemplate) {
            Request request = new Request.Builder()
                    .url(templateService)
                    .post(RequestBody.create(MEDIA_TYPE_HTML, body))
                    .removeHeader("Content-Type")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    throw new GraphQLException("Error sending email");
                }
                return response.body().string();
            } catch (Exception e) {
                e.printStackTrace();
                throw new GraphQLException(e);
            }
        } else {
            return body;
        }
    }

    public void sendEmail(final String groupId, final String subject, final String body, final String email, final Map<String, Object> parameters) {

        final InvitationDto emailDto = new InvitationDto();
        emailDto.setSubject(subject);
        emailDto.setBody(body);
        emailDto.setRecipients(ImmutableList.of(RecipientDto.builder()
                .groupId(groupId)
                .email(email)
                .parameters(parameters)
                .build()));

        final RestTemplate restTemplate = new RestTemplate();
        final ResultDetailsDto results = restTemplate.postForObject(url, emailDto, ResultDetailsDto.class);
        log.trace("Mail request was sent with the result: {}", results);
    }

    public void sendResetPasswordEmail(final User user, final String locale) {
        log.debug("Will send reset password email for user: {}", user.getUsername());

        final String token = tokenService.generatePasswordResetToken(user.getId());
        final String expirationTimeText = multilingualService.prettyTextOf(tokenService.getResetTokenExpirationDuration(), locale);

        final String resetLink = UriComponentsBuilder
                .fromHttpUrl(adminAppUrl)
                .pathSegment(RESET_PASSWORD_URI_PATH, token).build().toUriString();
        final String subject = multilingualService.lookupPhrase(RESET_PASSWORD_MAIL_SUBJECT_MULTILINGUAL_KEY, locale, DEFAULT_RESET_PASSWORD_MAIL_SUBJECT);
        final String body = wrapBody(multilingualService.lookupPhrase(RESET_PASSWORD_MAIL_BODY_MULTILINGUAL_KEY, locale, DEFAULT_RESET_PASSWORD_MAIL_BODY));

        sendEmail(RESET_PASSWORD_GROUP_ID, subject, body, user.getEmail(), ImmutableMap.of(
                "reset_link", resetLink,
                "reset_link_expiration_time", expirationTimeText));
    }

    public void sendActivationEmail(final User user, final String locale) {
        log.debug("Will send activation email for user: {}", user.getUsername());

        final String token = tokenService.generateUserActivationToken(user.getId());
        final String expirationTimeText = multilingualService.prettyTextOf(tokenService.getActivationTokenDuration(), locale);

        final String resetLink = UriComponentsBuilder
                .fromHttpUrl(adminAppUrl)
                .pathSegment(RESET_PASSWORD_URI_PATH, token).build().toUriString();
        final String subject = multilingualService.lookupPhrase(ACTIVATION_MAIL_SUBJECT_MULTILINGUAL_KEY, locale, DEFAULT_ACTIVATION_MAIL_SUBJECT);
        final String body = wrapBody(multilingualService.lookupPhrase(ACTIVATION_MAIL_BODY_MULTILINGUAL_KEY, locale, DEFAULT_ACTIVATION_MAIL_BODY));

        sendEmail(RESET_PASSWORD_GROUP_ID, subject, body, user.getEmail(), ImmutableMap.of(
                "reset_link", resetLink,
                "reset_link_expiration_time", expirationTimeText,
                "username", user.getUsername()));
    }
}
