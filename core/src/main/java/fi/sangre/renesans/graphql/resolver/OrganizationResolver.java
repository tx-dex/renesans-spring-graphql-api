package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor

@Component
public class OrganizationResolver implements GraphQLResolver<Organization> {
    private final OrganizationService organizationService;
    private final QuestionService questionService;

    @Nullable
    public Segment getSegment(@NonNull final Organization organization) {
        return organizationService.getSegment(organization);
    }

    @NonNull
    public OrganizationSurvey getSurvey(@NonNull final Organization organization) {
        return organizationService.getSurvey(organization);
    }

    @NonNull
    public List<CatalystDto> getCatalysts(@NonNull final Organization organization) {
        return questionService.getCatalysts(organization);
    }
}
