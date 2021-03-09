package fi.sangre.renesans.model.excel;

import fi.sangre.renesans.application.model.ParameterId;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ExcelParameterColumn implements ExcelColumn {
    private int columnIndex;
    private String columnName;
    private ParameterId parameterId;
}
