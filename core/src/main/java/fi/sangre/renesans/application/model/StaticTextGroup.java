package fi.sangre.renesans.application.model;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "id")
@Builder
public class StaticTextGroup {
    private String id;
    private String title;
    private String description;
    private List<StaticText> texts;
}
