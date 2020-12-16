package fi.sangre.renesans.application.dao;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.RespondentCounterAssembler;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.assemble.SurveyAssembler;
import fi.sangre.renesans.persistence.model.RespondentStateCounters;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyDao {
    private final CustomerRepository customerRepository;
    private final SurveyRepository surveyRepository;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final RespondentCounterAssembler respondentCounterAssembler;
    private final SurveyAssembler surveyAssembler;

    @NonNull
    @Transactional(readOnly = true)
    public List<OrganizationSurvey> getSurveys(@NonNull final OrganizationId organizationId, @NonNull final String languageTag) {
        return customerRepository.findById(organizationId.getValue())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"))
                .getSurveys().stream()
                .map(organizationSurveyAssembler::from)
                .sorted((e1, e2) -> compare(e1.getTitles().getPhrases(), e2.getTitles().getPhrases(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey getSurveyOrThrow(@NonNull final SurveyId surveyId) {
        return surveyRepository.findById(surveyId.getValue())
                .map(organizationSurveyAssembler::from)
                .orElseThrow(() -> new SurveyException("Survey not found"));
    }

    @Nullable
    @Transactional(readOnly = true)
    public OrganizationSurvey getSurveyOrNull(@NonNull final SurveyId surveyId) {
        return surveyRepository.findById(surveyId.getValue())
                .map(organizationSurveyAssembler::from)
                .orElse(null);
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<SurveyId, RespondentCounters> countRespondents() {
        return surveyRespondentRepository.countSurveyRespondents().stream()
                .collect(collectingAndThen(toMap(
                        RespondentStateCounters::getSurveyId,
                        respondentCounterAssembler::from
                ), Collections::unmodifiableMap));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<SurveyId, RespondentCounters> countRespondents(@NonNull final OrganizationId organizationId) {
        final Set<UUID> surveyIds = surveyRepository.findAllByOrganisationsIdIn(ImmutableSet.of(organizationId.getValue())).stream()
                .map(Survey::getId)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));

        return surveyRespondentRepository.countSurveyRespondents(surveyIds).stream()
                .collect(collectingAndThen(toMap(
                        RespondentStateCounters::getSurveyId,
                        respondentCounterAssembler::from
                ), Collections::unmodifiableMap));
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
