package fi.sangre.renesans.application.model;

import fi.sangre.renesans.persistence.model.User;
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
    private User owner; //TODO: change to intermediate type not a persistence one
}
