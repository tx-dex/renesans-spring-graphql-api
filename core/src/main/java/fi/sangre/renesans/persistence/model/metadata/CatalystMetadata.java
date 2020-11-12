package fi.sangre.renesans.persistence.model.metadata;

import fi.sangre.renesans.graphql.output.CatalystOutput;
import lombok.*;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(of = "pdfName")
@ToString
@Builder
public class CatalystMetadata implements Serializable, CatalystOutput {
    private Long id;
    private String pdfName;
    private Map<String, String> titles;
    private List<DriverMetadata> drivers;
    @Builder.Default
    private Double weight = 0.5;
}
