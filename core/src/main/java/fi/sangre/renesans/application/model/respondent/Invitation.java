package fi.sangre.renesans.application.model.respondent;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "email")
@Builder
public class Invitation {
    private String email;
}
