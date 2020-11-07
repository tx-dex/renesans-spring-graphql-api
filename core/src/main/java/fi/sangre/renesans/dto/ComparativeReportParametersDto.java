package fi.sangre.renesans.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ComparativeReportParametersDto extends ReportParametersDto {
    private List<Long> customerIds = new ArrayList<>();
    private List<String> respondentGroupIds = new ArrayList<>();
    private List<String> respondentIds = new ArrayList<>();
}
