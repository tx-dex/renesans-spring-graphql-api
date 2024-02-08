package fi.sangre.renesans.aaa;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.QuestionGroup;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j
@Service
public class SurveyMappingService {
    private final SurveyRepository surveyRepository;
    private final SurveyUtils surveyUtils;

    @NonNull
    @Transactional(readOnly = true)
    public Set<UUID> getSurveyOrganizations(@NonNull final UUID surveyId) {
        return surveyRepository.findById(surveyId)
                .map(Survey::getOrganisations)
                .orElse(ImmutableSet.of())
                .stream()
                .map(Customer::getId)
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    public Set<UUID> getSurveyQuestions(@NonNull final UUID surveyId) {
        return surveyRepository.findById(surveyId)
                .orElseThrow(() -> new RuntimeException("No survey found with id " + surveyId))
                .getMetadata().getCatalysts()
                .stream()
                .flatMap(catalystMetadata -> catalystMetadata.getQuestions()
                        .stream().map(QuestionMetadata::getId))
                .collect(Collectors.toSet());
    }

    public Set<UUID> getOrganizationsSurveys(@NonNull final Set<UUID> organizationIds) {
        return surveyRepository.findAllByOrganisationsIdIn(organizationIds).stream().map(Survey::getId).collect(toSet());
    }
}
