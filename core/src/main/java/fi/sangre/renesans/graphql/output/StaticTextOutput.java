package fi.sangre.renesans.graphql.output;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "title")
@Builder
public class StaticTextOutput {
    private String id;
    private String title;
    private String description;
    private String text;
}
