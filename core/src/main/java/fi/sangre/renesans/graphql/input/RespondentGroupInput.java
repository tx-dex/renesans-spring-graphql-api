package fi.sangre.renesans.graphql.input;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class RespondentGroupInput {
    private String id;
    private String title;
    private String description;
    private UUID customerId;
    private String surveyId;
    private List<Long> questionGroupIds;
    private String defaultLocale;
}
