package fi.sangre.renesans.graphql.output;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class AuthorizationOutput {
    private String token;
}
