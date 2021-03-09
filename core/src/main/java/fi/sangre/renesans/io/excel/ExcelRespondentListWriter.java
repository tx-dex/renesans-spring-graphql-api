package fi.sangre.renesans.io.excel;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.output.parameter.RespondentParameterAnswerOutput;
import fi.sangre.renesans.model.excel.ExcelColumn;
import fi.sangre.renesans.model.excel.ExcelFieldColumn;
import fi.sangre.renesans.model.excel.ExcelParameterColumn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class ExcelRespondentListWriter {
    private static final String EMPTY = "";

    public void write(@NonNull final Map<ParameterId, String> parameters,
                      @NonNull final Collection<RespondentOutput> respondents,
                      @NonNull final OutputStream outputStream) throws Exception {

        try (final Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Sheet1");

            final List<ExcelColumn> columns = prepareHeader(parameters);
//            final Map<String, ExcelRespondent> rows = prepareRows(respondents);

            writeHeader(workbook, sheet, columns);
            writeRows(workbook, sheet, columns, respondents);

            workbook.write(outputStream);
        } catch (final Exception ex) {
            log.warn("cannot write respondent list", ex);
            throw new SurveyException("Cannot write respondent list");
        }
    }

    @NonNull
    private List<ExcelColumn> prepareHeader(@NonNull final Map<ParameterId, String> parameters) {
        final ImmutableList.Builder<ExcelColumn> builder = ImmutableList.builder();

        int columnIndex = 0;
        builder.add(new ExcelFieldColumn(columnIndex++, "Email"));
        builder.add(new ExcelFieldColumn(columnIndex++, "Status"));

        for (final Map.Entry<ParameterId, String> parameter : parameters.entrySet()) {
            builder.add(new ExcelParameterColumn(columnIndex++, parameter.getValue(), parameter.getKey()));
        }

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
        headerCellStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        headerCellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        {
            final Row headerRow = sheet.createRow(0);
            final Cell cell1 = headerRow.createCell(0);
            cell1.setCellValue("Respondents");
            sheet.addMergedRegion(CellRangeAddress.valueOf("A1:B1"));
            cell1.setCellStyle(headerCellStyle);

            final Cell cell2 = headerRow.createCell(2);
            cell2.setCellValue("Parameters");
            cell2.setCellStyle(headerCellStyle);
        }

        {
            final Row headerRow = sheet.createRow(1);
            for (final ExcelColumn column : columns) {
                final Cell cell = headerRow.createCell(column.getColumnIndex());
                cell.setCellValue(column.getColumnName());
                cell.setCellStyle(headerCellStyle);
                sheet.autoSizeColumn(column.getColumnIndex());
            }
        }
    }

    private void writeRows(@NonNull final Workbook workbook,
                           @NonNull final Sheet sheet,
                           @NonNull final List<ExcelColumn> columns,
                           @NonNull final Collection<RespondentOutput> respondents) {
        int rowNum = 2;

        for (final RespondentOutput respondent : respondents) {
            final Row row = sheet.createRow(rowNum++);

            int columnNum = 0;
            writeCellValue(row, columns.get(columnNum++), respondent.getEmail());
            writeCellValue(row, columns.get(columnNum++), respondent.getState());

            final Map<ParameterId, String> answers = Optional.ofNullable(respondent.getParameterAnswers())
                    .orElse(ImmutableList.of()).stream()
                    .collect(toMap(RespondentParameterAnswerOutput::getId, RespondentParameterAnswerOutput::getResponse));

            for (int i = columnNum; i < columns.size(); i++) {
                final ExcelColumn column = columns.get(i);
                if (column instanceof ExcelParameterColumn) {
                    final ParameterId parameterId = ((ExcelParameterColumn) column).getParameterId();
                    writeCellValue(row, column, answers.getOrDefault(parameterId, EMPTY));
                }
            }
        }
    }

    private void writeCellValue(final Row row, final ExcelColumn column, final Object value) {
        final Cell cell = row.createCell(column.getColumnIndex());
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else if (value instanceof String) {
                cell.setCellValue((String) value);
            } else {
                cell.setCellValue(value.toString());
            }
        }
    }
}
