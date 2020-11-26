package fi.sangre.renesans.aaa;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j
@Service
public class SurveyMappingService {
    private final SurveyRepository surveyRepository;

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
}
