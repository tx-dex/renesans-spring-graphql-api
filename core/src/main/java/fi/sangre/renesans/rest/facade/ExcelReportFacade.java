package fi.sangre.renesans.rest.facade;

import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.io.excel.ExcelRespondentListWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.OutputStream;

@RequiredArgsConstructor
@Slf4j

@Component
public class ExcelReportFacade {
    private final ExcelRespondentListWriter excelRespondentListWriter;

    public void writeRespondentsList(@NonNull final SurveyId surveyId, @NonNull final OutputStream output) throws Exception {

        excelRespondentListWriter.write(output);

    }
}
