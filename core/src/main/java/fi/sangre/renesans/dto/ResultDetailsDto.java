package fi.sangre.renesans.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ResultDetailsDto {

    private int sentCount;
    private int failedCount;
    private List<RecipientDto> recipients;
}
