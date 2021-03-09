package fi.sangre.renesans.io.excel;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.model.excel.ExcelColumn;
import fi.sangre.renesans.model.excel.ExcelFieldColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class ExcelRespondentListWriter {

    public void write(@NonNull final OutputStream outputStream) throws Exception {

        try (final Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            final List<ExcelColumn> columns = prepareHeader();
//            final Map<String, ExcelRespondent> rows = prepareRows(respondents);

            writeHeader(workbook, sheet, columns);
//            writeRows(workbook, sheet, columns, rows.values());

            workbook.write(outputStream);
        } catch (final Exception ex) {
            log.warn("cannot write respondent list", ex);
            throw new SurveyException("Cannot write respondent list");
        }
    }


    private List<ExcelColumn> prepareHeader() {
        final ImmutableList.Builder<ExcelColumn> builder = ImmutableList.builder();

        int columnIndex = 0;
        builder.add(new ExcelFieldColumn(columnIndex++, "Respondents"));
        builder.add(new ExcelFieldColumn(columnIndex++, "Status"));

        return builder.build();
    }

    private void writeHeader(@NonNull final Workbook workbook,
                             @NonNull final Sheet sheet,
                             @NonNull final List<ExcelColumn> columns) {
        final Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);

        final CellStyle headerCellStyle = workbook.createCellStyle();
        headerCellStyle.setFont(headerFont);
        headerCellStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.DIAMONDS);

        final Row headerRow = sheet.createRow(0);
        for(final ExcelColumn column : columns) {
            Cell cell = headerRow.createCell(column.getColumnIndex());
            cell.setCellValue(column.getColumnName());
            cell.setCellStyle(headerCellStyle);
            sheet.autoSizeColumn(column.getColumnIndex());
        }
    }
}
