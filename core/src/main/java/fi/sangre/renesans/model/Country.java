package fi.sangre.renesans.model;

import lombok.*;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Country {
    private String name;
    private String code;
}
