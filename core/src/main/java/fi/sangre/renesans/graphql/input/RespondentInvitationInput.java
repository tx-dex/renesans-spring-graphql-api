package fi.sangre.renesans.graphql.input;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class RespondentInvitationInput {
    private String subject;
    private String body;
    private List<String> emails;
}
