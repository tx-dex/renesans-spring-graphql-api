package fi.sangre.renesans.dto;

import fi.sangre.renesans.graphql.output.DriverOutput;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverDto implements DriverOutput {
    private Long id;
    private String pdfName;
    private Long titleId;
    private Long descriptionId;
    private Long prescriptionId;
    private Double weight;
    private Long catalystId;
}
