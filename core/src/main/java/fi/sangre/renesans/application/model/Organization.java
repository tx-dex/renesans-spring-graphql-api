package fi.sangre.renesans.application.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class Organization {
    private Long id;
    private String name;
    private String description;
}
