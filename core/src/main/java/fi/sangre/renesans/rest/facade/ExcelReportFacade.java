package fi.sangre.renesans.rest.facade;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.ParameterUtils;
import fi.sangre.renesans.graphql.facade.SurveyRespondentsFacade;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.io.excel.ExcelRespondentListWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class ExcelReportFacade {
    private static final String EN = "en";
    private static final String EMPTY = "";

    private final ExcelRespondentListWriter excelRespondentListWriter;

    private final SurveyDao surveyDao;
    private final SurveyRespondentsFacade surveyRespondentsFacade;
    private final ParameterUtils parameterUtils;
    private final MultilingualUtils multilingualUtils;


    public void writeRespondentsList(@NonNull final SurveyId surveyId, @NonNull final OutputStream output) throws Exception {
        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);
        final Collection<RespondentOutput> respondents = surveyRespondentsFacade.getSurveyRespondents(surveyId, ImmutableList.of(), EN);

        final Map<ParameterId, String> parameters = Optional.ofNullable(survey.getParameters())
                .orElse(ImmutableList.of()).stream()
                .collect(toMap(Parameter::getId, v -> multilingualUtils.getTextOrDefault(v.getLabel(), EN, EMPTY)));

        excelRespondentListWriter.write(parameters, respondents, output);

    }
}
