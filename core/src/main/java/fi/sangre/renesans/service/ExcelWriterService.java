package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.LocalizedCatalyst;
import fi.sangre.renesans.application.model.LocalizedDriver;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.excel.*;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.statistics.Statistics;
import fi.sangre.renesans.statistics.StatisticsCatalyst;
import fi.sangre.renesans.statistics.StatisticsDriver;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@Service
public class ExcelWriterService {
    private static final String EXCEL_LANGUAGE = "en";

    private static final String GROUP_HEADER = "Group";
    private static final String GENDER_HEADER = "Gender";
    private static final String AGE_HEADER = "Age";
    private static final String EXPERIENCE_HEADER = "Experience";
    private static final String POSITION_HEADER = "Position";
    private static final String INDUSTRY_HEADER = "Industry";
    private static final String SEGMENT_HEADER = "Segment";
    private static final String LOCATION_HEADER = "Location";

    private final StatisticsService statisticsService;
    private final RespondentService respondentService;
    private final MultilingualService multilingualService;

    @Autowired
    public ExcelWriterService(final StatisticsService statisticsService,
                              final RespondentService respondentService,
                              final MultilingualService multilingualService) {
        this.statisticsService = statisticsService;
        this.respondentService = respondentService;
        this.multilingualService = multilingualService;
    }

    public void excelGenerator(final Customer customer, final OutputStream outputStream) throws Exception {
        final List<Respondent> respondents = respondentService.getFinishedRespondentsByCustomer(customer);

        final Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Sheet1");

        final List<ExcelColumn> columns = prepareHeader(customer);
        final Map<String, ExcelRespondent> rows = prepareRows(respondents);

        writeHeader(workbook, sheet, columns);
        writeRows(workbook, sheet, columns, rows.values());

        workbook.write(outputStream);
        workbook.close();
    }

    private List<ExcelColumn> prepareHeader(final Customer customer) {
        final ImmutableList.Builder<ExcelColumn> builder = ImmutableList.builder();

        int columnIndex = 0;
        builder.add(new ExcelFieldColumn(columnIndex++, GROUP_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, GENDER_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, AGE_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, EXPERIENCE_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, POSITION_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, INDUSTRY_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, SEGMENT_HEADER));
        builder.add(new ExcelFieldColumn(columnIndex++, LOCATION_HEADER));

        final List<LocalizedCatalyst> catalysts = statisticsService.getCatalysts(customer, EXCEL_LANGUAGE);

        for (final LocalizedCatalyst catalyst : catalysts) {
            builder.add(new ExcelCatalystColumn(columnIndex++, catalyst.getName(), catalyst.getId()));
        }

        for (final LocalizedCatalyst catalyst : catalysts) {
            for (final LocalizedDriver driver : catalyst.getDrivers()) {
                builder.add(new ExcelDriverColumn(columnIndex++, driver.getName(), driver.getId()));
            }
        }

        return builder.build();
    }

    private Map<String, ExcelRespondent> prepareRows(final List<Respondent> respondents) {
        return respondents.stream().map(e -> {
            final ExcelRespondent excelRespondent = ExcelRespondent.builder()
                    .id(e.getId())
                    .values(prepareFieldData(e))
                    .build();

            prepareStatisticsData(excelRespondent, e);

            return excelRespondent;
        }).collect(collectingAndThen(toMap(ExcelRespondent::getId, e -> e), Collections::unmodifiableMap));
    }

    private Map<String, Object> prepareFieldData(final Respondent respondent) {
        final Map<String, Object> values = new HashMap<>();

        values.put(GROUP_HEADER, respondent.getRespondentGroup().getTitle());
        values.put(GENDER_HEADER, respondent.getGender());
        values.put(AGE_HEADER, respondent.getAge());
        values.put(EXPERIENCE_HEADER, respondent.getExperience());
        values.put(POSITION_HEADER, multilingualService.lookupPhrase(respondent.getPosition().getTitle(), EXCEL_LANGUAGE));
        values.put(INDUSTRY_HEADER, multilingualService.lookupPhrase(respondent.getIndustry().getTitle(), EXCEL_LANGUAGE));
        values.put(SEGMENT_HEADER, multilingualService.lookupPhrase(respondent.getSegment().getTitle(), EXCEL_LANGUAGE));
        values.put(LOCATION_HEADER, new Locale(EXCEL_LANGUAGE, respondent.getCountry()).getDisplayCountry());

        return values;
    }

    private void prepareStatisticsData(final ExcelRespondent excelRespondent, final Respondent respondent) {
        final Map<Long, Double> catalystResults = new HashMap<>();
        final Map<Long, Double> driversresults = new HashMap<>();
        final Statistics statistics = statisticsService.calculateStatisticForRespondent(respondent);

        for(final StatisticsCatalyst catalyst : statistics.getCatalysts()) {
            catalystResults.put(catalyst.getId(), catalyst.getWeighedResult());

            for(final StatisticsDriver driver : catalyst.getDevelopmentTrackIndices() ) {
                driversresults.put(driver.getId(), driver.getWeighedResult());
            }

        }

        excelRespondent.setCatalystsIndices(catalystResults);
        excelRespondent.setDriversIndices(driversresults);
    }

    private void writeHeader(final Workbook workbook, final Sheet sheet, final List<ExcelColumn> columns) {
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 14);


        CellStyle headerCellStyle = workbook.createCellStyle();
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

    private void writeRows(final Workbook workbook, final Sheet sheet, final List<ExcelColumn> columns, final Collection<ExcelRespondent> respondents) {
        int rowNum = 1;

        final CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("0%"));


        for (final ExcelRespondent respondent : respondents) {
            Row row = sheet.createRow(rowNum++);

            for (final ExcelColumn column : columns) {
                if (column instanceof ExcelFieldColumn) {
                    writeCellValue(row, column, respondent.getValues().get(column.getColumnName()));
                } else if (column instanceof ExcelCatalystColumn) {
                    writeCatalystCells(row, (ExcelCatalystColumn) column, respondent.getCatalystsIndices()).setCellStyle(style);
                } else if (column instanceof ExcelDriverColumn) {
                    writeDriverCells(row, (ExcelDriverColumn) column, respondent.getDriversIndices()).setCellStyle(style);
                }
            }
        }
    }

    private Cell writeCellValue(final Row row, final ExcelColumn column, final Object value) {
        final Cell cell = row.createCell(column.getColumnIndex());
        if (value != null) {
            if (value instanceof Number) {
                cell.setCellValue(((Number) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        }
        return cell;
    }

    private Cell writeCatalystCells(final Row row, final ExcelCatalystColumn column, final Map<Long, Double> catalysts) {
        final Cell cell = row.createCell(column.getColumnIndex());
        final Double value = catalysts.get(column.getCatalystId());
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }

    private Cell writeDriverCells(final Row row, final ExcelDriverColumn column, final Map<Long, Double> drivers) {
        final Cell cell = row.createCell(column.getColumnIndex());
        final Double value = drivers.get(column.getDriverId());
        if (value != null) {
            cell.setCellValue(value);
        }
        return cell;
    }
}

