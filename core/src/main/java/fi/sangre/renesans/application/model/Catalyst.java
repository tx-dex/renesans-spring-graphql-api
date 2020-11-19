package fi.sangre.renesans.application.model;

import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.graphql.output.CatalystOutput;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class Catalyst implements CatalystOutput {
    private Long id;
    private String pdfName;
    private MultilingualText titles;
    private List<Driver> drivers;
    private List<LikertQuestion> questions;
    private Double weight;
}
