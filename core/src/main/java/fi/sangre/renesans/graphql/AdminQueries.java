package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyTemplate;
import fi.sangre.renesans.graphql.facade.SurveyRespondentsFacade;
import fi.sangre.renesans.graphql.input.FilterInput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.TemplateService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminQueries implements GraphQLQueryResolver {
    private final OrganizationService organizationService;
    private final OrganizationSurveyService organizationSurveyService;
    private final SurveyRespondentsFacade surveyRespondentsFacade;
    private final TemplateService templateService;
    private final ResolverHelper resolverHelper;

    @NonNull
    // TODO: authorize
    @PreAuthorize("isAuthenticated()")
    public List<Organization> getOrganizations() {
        return organizationService.findAll();
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'organization', 'READ')")
    public Organization getOrganization(@NonNull final UUID id) {
        return organizationService.findOrganization(id);
    }

    @NonNull
    // TODO: authorize
    @PreAuthorize("isAuthenticated()")
    public List<SurveyTemplate> getSurveyTemplates(@Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return templateService.getTemplates(resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'READ')")
    public OrganizationSurvey getOrganizationSurvey(@NonNull final UUID id, @Nullable final String languageCode, @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyService.getSurvey(id);
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'READ')")
    public Collection<RespondentOutput> getSurveyRespondents(@NonNull final UUID surveyId,
                                                             @Nullable final List<FilterInput> filters,
                                                             @Nullable final String languageCode,
                                                             @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return surveyRespondentsFacade.getSurveyRespondents(surveyId, filters, resolverHelper.getLanguageCode(environment));
    }
}
