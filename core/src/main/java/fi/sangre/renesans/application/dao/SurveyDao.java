package fi.sangre.renesans.application.dao;

import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.assemble.SurveyAssembler;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyDao {
    private final SurveyRepository surveyRepository;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final SurveyAssembler surveyAssembler;

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey getSurveyOrThrow(@NonNull final SurveyId surveyId) {
        return organizationSurveyAssembler.from(surveyRepository.findById(surveyId.getValue())
        .orElseThrow(() -> new SurveyException("Survey not found")));
    }

    @NonNull
    @Transactional
    public OrganizationSurvey store(@NonNull final OrganizationSurvey survey) {
        final Survey entity = surveyRepository.findById(survey.getId())
                .map(e -> surveyAssembler.from(e, survey))
                .orElseGet(() -> surveyAssembler.from(survey));

        return store(entity);
    }

    @NonNull
    @Transactional
    public OrganizationSurvey store(@NonNull final Survey entity) {
        return organizationSurveyAssembler.from(surveyRepository.saveAndFlush(entity));
    }
}
