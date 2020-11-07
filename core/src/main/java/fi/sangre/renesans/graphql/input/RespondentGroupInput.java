package fi.sangre.renesans.graphql.input;

import lombok.Data;

import java.util.List;

@Data
public class RespondentGroupInput {
    private String id;
    private String title;
    private String description;
    private Long customerId;
    private String surveyId;
    private List<Long> questionGroupIds;
    private String defaultLocale;
}
