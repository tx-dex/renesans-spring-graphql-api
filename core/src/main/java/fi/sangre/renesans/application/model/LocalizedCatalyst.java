package fi.sangre.renesans.application.model;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class LocalizedCatalyst {
    private Long id;
    private String name;
    private List<LocalizedDriver> drivers;
}
