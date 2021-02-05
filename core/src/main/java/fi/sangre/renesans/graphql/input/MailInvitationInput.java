package fi.sangre.renesans.graphql.input;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class MailInvitationInput {
    private String subject;
    private String body;
    private Boolean inviteAll;
    private List<String> emails;
    private String invitationLanguage;
}
