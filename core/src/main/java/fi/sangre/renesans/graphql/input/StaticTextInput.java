package fi.sangre.renesans.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StaticTextInput {
    private String id;
    private String textGroupId;
    private String text;
}
