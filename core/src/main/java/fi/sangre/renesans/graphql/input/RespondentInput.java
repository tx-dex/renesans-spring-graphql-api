package fi.sangre.renesans.graphql.input;

import lombok.Data;

@Data
public class RespondentInput {
    private String id;
    private String name;
    private String email;
    private Long age;
    private Long position;
    private Long industry;
    private Long segment;
    private String phone;
    private String gender;
    private String country;
    private Long experience;
    private Boolean consent;
    private String locale;
}

