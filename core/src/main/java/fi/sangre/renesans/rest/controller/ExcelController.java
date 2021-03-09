package fi.sangre.renesans.rest.controller;

import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.rest.facade.ExcelReportFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@RestController
public class ExcelController {
    private final ExcelReportFacade excelReportFacade;

    @GetMapping("/survey/report/{surveyId}/respondents/excel")
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'READ')")
    public void downloadExcelFile(@NonNull @PathVariable final UUID surveyId, @NonNull final HttpServletResponse response) throws Exception {
        final OutputStream output = response.getOutputStream();
        try {
            response.setHeader("Content-Disposition", "attachment; filename=\"respondents.xlsx\"");
            response.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

            excelReportFacade.writeRespondentsList(new SurveyId(surveyId), output);
        } finally {
            IOUtils.closeQuietly(output, ex -> log.warn("Cannot close stream", ex));
        }
    }
}
