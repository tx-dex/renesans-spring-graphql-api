package fi.sangre.renesans.graphql.assemble;

import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.RespondentCounters;
import fi.sangre.renesans.application.model.SurveyCounters;
import fi.sangre.renesans.graphql.assemble.aaa.UserOutputAssembler;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class OrganizationOutputAssembler {
    private final UserOutputAssembler userOutputAssembler;

    @NonNull
    public Collection<OrganizationOutput> from(@NonNull final List<Organization> organizations,
                                               @NonNull final Map<OrganizationId, RespondentCounters> respondents,
                                               @NonNull final Map<OrganizationId, SurveyCounters> surveys) {
        return organizations.stream()
                .map(e -> OrganizationOutput.builder()
                        .id(e.getId())
                        .name(e.getName())
                        .description(e.getDescription())
                        .respondentCounters(respondents.getOrDefault(new OrganizationId(e.getId()), RespondentCounters.empty()))
                        .surveyCounters(surveys.getOrDefault(new OrganizationId(e.getId()), SurveyCounters.empty()))
                        .owner(Optional.ofNullable(e.getOwner())
                                .map(userOutputAssembler::from)
                                .orElse(null))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public Collection<OrganizationOutput> from(@NonNull final Collection<Organization> organizations) {
        return organizations.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public OrganizationOutput from(@NonNull final Organization organization) {
        return OrganizationOutput.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .owner(Optional.ofNullable(organization.getOwner())
                        .map(userOutputAssembler::from)
                        .orElse(null))
                .respondentCounters(null)
                .surveyCounters(null) //TODO: add survey counters
                .build();
    }

}
