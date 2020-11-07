package fi.sangre.renesans.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReportParametersDto {
    private FiltersBaseDto filters;
    private PartnerDetailsDto partnerDetails;
    private String languageCode = "en";
    private Boolean edit = false;
}
