package fi.sangre.renesans.service;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.sangre.mail.dto.MailDto;
import com.sangre.mail.dto.NameEmailPairDto;
import com.sangre.mail.dto.attachements.Base64AttachmentDto;
import fi.sangre.renesans.application.client.FeignMailClient;
import fi.sangre.renesans.application.event.ActivateUserEvent;
import fi.sangre.renesans.application.event.RequestUserPasswordResetEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@RequiredArgsConstructor
@Slf4j
@Service
public class MailService {
    private static final String RESET_PASSWORD_URI_PATH = "reset";
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
    private final FeignMailClient feignMailClient;
    private final TemplateService templateService;

    @Value("${fi.sangre.renesans.admin.url}")
    private String adminAppUrl;

    @TransactionalEventListener
    public void sendResetPasswordEmail(@NonNull final RequestUserPasswordResetEvent event) {
        log.debug("Will send reset password email for user: {}", event.getUsername());

        final String token = tokenService.generatePasswordResetToken(event.getUserId());
        final String expirationTimeText = multilingualService.prettyTextOf(tokenService.getResetTokenExpirationDuration(), event.getLocale());

        final String resetLink = UriComponentsBuilder
                .fromHttpUrl(adminAppUrl)
                .pathSegment(RESET_PASSWORD_URI_PATH, token).build().toUriString();
        final String subject = multilingualService.lookupPhrase(RESET_PASSWORD_MAIL_SUBJECT_MULTILINGUAL_KEY, event.getLocale(), DEFAULT_RESET_PASSWORD_MAIL_SUBJECT);
        final String body = templateService.templateBody(multilingualService.lookupPhrase(RESET_PASSWORD_MAIL_BODY_MULTILINGUAL_KEY,
                event.getLocale(),
                DEFAULT_RESET_PASSWORD_MAIL_BODY), ImmutableMap.of(
                "reset_link", resetLink,
                "reset_link_expiration_time", expirationTimeText));

        sendEmail(event.getEmail(), subject, body, ImmutableMap.of("email-type", "reset-password"));
    }

    @TransactionalEventListener
    public void sendActivationEmail(@NonNull final ActivateUserEvent event) {
        log.debug("Will send activation email for user: {}", event.getUsername());

        final String token = tokenService.generateUserActivationToken(event.getUserId());
        final String expirationTimeText = multilingualService.prettyTextOf(tokenService.getActivationTokenDuration(), event.getLocale());

        final String resetLink = UriComponentsBuilder
                .fromHttpUrl(adminAppUrl)
                .pathSegment(RESET_PASSWORD_URI_PATH, token).build().toUriString();
        final String subject = multilingualService.lookupPhrase(ACTIVATION_MAIL_SUBJECT_MULTILINGUAL_KEY, event.getLocale(), DEFAULT_ACTIVATION_MAIL_SUBJECT);
        final String body = templateService.templateBody(multilingualService.lookupPhrase(ACTIVATION_MAIL_BODY_MULTILINGUAL_KEY,
                event.getLocale(),
                DEFAULT_ACTIVATION_MAIL_BODY),
                ImmutableMap.of(
                        "reset_link", resetLink,
                        "reset_link_expiration_time", expirationTimeText,
                        "username", event.getUsername()));

        sendEmail(event.getEmail(), subject, body, ImmutableMap.of("email-type", "activate-user"));
    }

    private void sendEmail(final String recipient, final String subject, final String body, final Map<String, String> tags) {
        log.trace("Sending mail with tags: {} to: {}", tags, recipient);

        final MailDto emailDto = MailDto.builder()
                .subject(subject)
                .htmlBody(body)
                .recipients(ImmutableList.of(recipient))
                .tags(tags)
                .build();

        feignMailClient.sendEmail(emailDto);
        log.debug("Sent mail with tags: {} to: {}", tags, recipient);
    }

    public void sendEmail(@NonNull final String recipient,
                          @NonNull final String subject,
                          @NonNull final String htmlBody,
                          @NonNull final String textBody,
                          @NonNull final Map<String, String> tags,
                          @NonNull final Pair<String, String> replyTo,
                          @NonNull final Base64AttachmentDto logo) {
        log.trace("Sending mail with tags: {} to: {}", tags, recipient);

        final String senderName = replyTo.getLeft() + " | Engager";
        final MailDto message = MailDto.builder()
                .recipients(ImmutableList.of(recipient))
                .subject(subject)
                .htmlBody(htmlBody)
                .textBody(textBody)
                .sender(NameEmailPairDto.builder()
                        .name(senderName)
                        .build())
                .replyTo(NameEmailPairDto.builder()
                        .name(senderName)
                        .email(replyTo.getRight())
                        .build())
                .attachments(ImmutableList.of(logo))
                .tags(tags)
                .build();

        feignMailClient.sendEmail(message);
        log.debug("Sent mail with tags: {} to: {}", tags, recipient);
    }
}
