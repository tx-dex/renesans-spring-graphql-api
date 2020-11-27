package fi.sangre.renesans.graphql.output;

import fi.sangre.renesans.application.model.MultilingualText;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionnaireDriverOutput {
    private Long id;
    private MultilingualText titles;
    private MultilingualText descriptions;
}
