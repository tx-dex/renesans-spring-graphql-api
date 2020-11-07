package fi.sangre.renesans.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder

public class Catalyst {
    Long id;
    String name;
    List<Driver> drivers;
}
