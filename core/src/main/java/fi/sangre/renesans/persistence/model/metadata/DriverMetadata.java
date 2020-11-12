package fi.sangre.renesans.persistence.model.metadata;

import fi.sangre.renesans.graphql.output.DriverOutput;
import lombok.*;

import java.io.Serializable;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "pdfName")
@ToString
@Builder
public class DriverMetadata implements Serializable, DriverOutput {
    private Long id;
    private String pdfName;
    private Map<String, String> titles;
    private Map<String, String> descriptions;
    private Map<String, String> prescriptions;
    @Builder.Default
    private Double weight = 0.5;
}
