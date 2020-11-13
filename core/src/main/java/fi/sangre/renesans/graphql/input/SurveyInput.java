package fi.sangre.renesans.graphql.input;

import lombok.Data;

import java.util.UUID;

@Data
public class SurveyInput {
    private UUID id;
    private Long version;
    private String title;
    private String description;
    private Long templateId;
}

