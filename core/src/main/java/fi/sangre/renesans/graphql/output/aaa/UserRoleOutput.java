package fi.sangre.renesans.graphql.output.aaa;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserRoleOutput {
    private String name;
    private String title;
}
