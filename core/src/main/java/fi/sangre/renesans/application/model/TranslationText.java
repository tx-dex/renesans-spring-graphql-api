package fi.sangre.renesans.application.model;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "title")
@Builder
public class TranslationText {
    private String title;
    private String description;
    private String text;
}
