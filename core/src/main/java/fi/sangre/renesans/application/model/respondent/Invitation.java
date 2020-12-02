package fi.sangre.renesans.application.model.respondent;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Invitation {
    private String subject;
    private String body;
    private Set<String> emails;
}
