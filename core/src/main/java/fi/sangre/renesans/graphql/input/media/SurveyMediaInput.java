package fi.sangre.renesans.graphql.input.media;

import lombok.Data;
import lombok.ToString;

import java.util.UUID;

@Data
@ToString
public class SurveyMediaInput {
    private UUID id;
    private String title;
    private MediaDetailsInput details;
}
