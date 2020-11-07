package fi.sangre.renesans.graphql.input;

import lombok.Data;

@Data
public class CustomerInput {
    private Long id;
    private String name;
    private String description;
    private Long segmentId;
}

