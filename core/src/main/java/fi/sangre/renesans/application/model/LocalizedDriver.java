package fi.sangre.renesans.application.model;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class LocalizedDriver {
    private Long id;
    private String name;
}
