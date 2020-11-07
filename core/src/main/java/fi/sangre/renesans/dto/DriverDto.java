package fi.sangre.renesans.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DriverDto {
    private Long id;
    private String pdfName;
    private Long titleId;
    private Long descriptionId;
    private Long prescriptionId;
    private Double weight;
    private Long catalystId;
}
