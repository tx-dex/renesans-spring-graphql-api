package fi.sangre.renesans.application.model;

import fi.sangre.renesans.graphql.output.DriverOutput;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder
public class Driver implements DriverOutput {
    private Long id;
    private String pdfName;
    private MultilingualText titles;
    private MultilingualText descriptions;
    private MultilingualText prescriptions;
    private Double weight;
}
