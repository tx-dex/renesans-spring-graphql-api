package fi.sangre.renesans.application.dao;

import fi.sangre.renesans.application.assemble.OrganizationAssembler;
import fi.sangre.renesans.application.assemble.RespondentCounterAssembler;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.OrganizationSurveyMapping;
import fi.sangre.renesans.persistence.model.SurveyStateCounters;
import fi.sangre.renesans.persistence.model.User;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationDao {
    private final UserRepository userRepository;
    private final SurveyDao surveyDao;
    private final CustomerRepository customerRepository;
    private final OrganizationAssembler organizationAssembler;
    private final RespondentCounterAssembler respondentCounterAssembler;

    @NonNull
    @Transactional(readOnly = true)
    public Organization getOrganizationOrThrow(@NonNull final OrganizationId id) {
        return customerRepository.findById(id.getValue())
                .map(organizationAssembler::from)
                .orElseThrow(() -> new SurveyException("Organization not found"));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<OrganizationId, RespondentCounters> countRespondents() {
        final Map<SurveyId, RespondentCounters> surveys = surveyDao.countRespondents();

        return customerRepository.getOrganizationSurveyMappings().stream()
                .collect(groupingBy(OrganizationSurveyMapping::getOrganizationId))
                .entrySet()
                .stream()
                .collect(collectingAndThen(toMap(
                        Map.Entry::getKey,
                        e -> respondentCounterAssembler.fromCounters(e.getValue().stream()
                                .map(m -> surveys.getOrDefault(m.getSurveyId(), RespondentCounters.empty()))
                                .collect(collectingAndThen(toList(), Collections::unmodifiableList)))

                ), Collections::unmodifiableMap));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<OrganizationId, SurveyCounters> countSurveys() {
        return customerRepository.countOrganizationSurveys().stream()
                .collect(collectingAndThen(toMap(
                        SurveyStateCounters::getId,
                        e -> SurveyCounters.builder()
                                .all(e.getAll())
                                .build()
                ), Collections::unmodifiableMap));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Collection<Organization> getUserOrganizations(@NonNull final Long userId) {
        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new SurveyException("User not found"));

        return customerRepository.findByUsersContainingOrCreatedBy(user, userId).stream()
                .map(organizationAssembler::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
