package fi.sangre.renesans.application.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class StaticText {
    private String id;
    private MultilingualText texts;
}
