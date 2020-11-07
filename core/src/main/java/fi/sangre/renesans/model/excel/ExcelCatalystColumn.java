package fi.sangre.renesans.model.excel;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ExcelCatalystColumn implements ExcelColumn {
    private int columnIndex;
    private String columnName;
    private Long catalystId;
}
