package fi.sangre.renesans.application.model;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@Data
@EqualsAndHashCode(of = "id")
@ToString
@Builder
public class Organization {
    private UUID id;
    private String name;
    private String description;
}
