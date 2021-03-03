package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.graphql.input.MailInvitationInput;
import fi.sangre.renesans.graphql.input.RespondentInvitationInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j

@Component
public class InvitationAssembler {

    @NonNull
    public Invitation from(@NonNull final MailInvitationInput input) {
        final Set<String> emails = Optional.ofNullable(input.getEmails())
                .orElse(ImmutableList.of())
                .stream()
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .map(StringUtils::lowerCase)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

        final boolean inviteAll = Boolean.TRUE.equals(input.getInviteAll());
        if (!inviteAll) {
            checkArgument(!emails.isEmpty(), "Non empty list of emails is required");
        }

        checkArgument(StringUtils.isNotBlank(input.getSubject()), "Subject is required");
        checkArgument(StringUtils.isNotBlank(input.getBody()), "Email body is required");

        return Invitation.builder()
                .subject(StringUtils.trim(input.getSubject()))
                .body(StringUtils.trim(input.getBody()))
                .emails(emails)
                .inviteAll(inviteAll)
                .language(input.getInvitationLanguage())
                .build();
    }

    @NonNull
    public Invitation from(@NonNull final RespondentInvitationInput input) {
        final Set<String> emails = Optional.ofNullable(input.getEmails())
                .orElse(ImmutableList.of())
                .stream()
                .map(StringUtils::trimToNull)
                .filter(Objects::nonNull)
                .map(StringUtils::lowerCase)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

        checkArgument(!emails.isEmpty(), "Non empty list of emails is required");
        checkArgument(StringUtils.isNotBlank(input.getSubject()), "Subject is required");
        checkArgument(StringUtils.isNotBlank(input.getBody()), "Email body is required");

        return Invitation.builder()
                .subject(StringUtils.trim(input.getSubject()))
                .body(StringUtils.trim(input.getBody()))
                .emails(emails)
                .inviteAll(null)
                .language(input.getInvitationLanguage())
                .build();
    }
}
