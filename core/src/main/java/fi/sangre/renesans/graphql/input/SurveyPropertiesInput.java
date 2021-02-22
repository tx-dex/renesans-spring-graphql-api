package fi.sangre.renesans.graphql.input;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyPropertiesInput {
    private Boolean hideCatalystThemePages;
}

