package fi.sangre.renesans.graphql.input;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class SurveyInput {
    private UUID id;
    private Long version;
    private String title;
    private String description;
    private SurveyPropertiesInput properties;
    private Long templateId;
    private List<String> languages;
    private UUID sourceSurveyId;
}

