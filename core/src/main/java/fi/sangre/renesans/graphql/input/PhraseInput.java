package fi.sangre.renesans.graphql.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhraseInput {
    private Long id;
    private String name;
    private String text;
    private String languageCode;
}

