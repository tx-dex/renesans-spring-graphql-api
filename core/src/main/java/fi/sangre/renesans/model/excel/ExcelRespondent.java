package fi.sangre.renesans.model.excel;

import lombok.*;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(of = "id")
@Builder

public class ExcelRespondent {
    private String id;
    private Map<String, Object> values;
    private Map<Long, Double> catalystsIndices;
    private Map<Long, Double> driversIndices;
}
