package fi.sangre.renesans.graphql.output.aaa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserOutput {
    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private boolean enabled;
    private Set<UserRoleOutput> roles;
}
