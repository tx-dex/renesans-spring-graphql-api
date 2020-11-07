package fi.sangre.renesans.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Data
public class BatchReportParametersDto {
    private String respondentGroupId;
    private List<String> respondentIds = new ArrayList<>();
    private PartnerDetailsDto partnerDetails;
    private String languageCode = "en";
}
