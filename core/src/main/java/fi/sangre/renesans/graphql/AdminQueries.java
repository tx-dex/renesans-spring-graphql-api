package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import fi.sangre.renesans.application.assemble.RespondentFilterAssembler;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.SurveyTemplate;
import fi.sangre.renesans.graphql.facade.OrganizationSurveyFacade;
import fi.sangre.renesans.graphql.facade.SurveyRespondentsFacade;
import fi.sangre.renesans.graphql.input.FilterInput;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
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
    private final OrganizationSurveyService organizationSurveyService;
    private final OrganizationSurveyFacade organizationSurveyFacade;
    private final SurveyRespondentsFacade surveyRespondentsFacade;
    private final RespondentFilterAssembler respondentFilterAssembler;
    private final TemplateService templateService;
    private final ResolverHelper resolverHelper;

    @NonNull
    @PreAuthorize("hasRole('SUPER_USER') or hasRole('POWER_USER')")
    public Collection<OrganizationOutput> getOrganizations() {
        return organizationSurveyFacade.getOrganizations();
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'organization', 'READ')")
    public OrganizationOutput getOrganization(@NonNull final UUID id) {
        return organizationSurveyFacade.getOrganization(new OrganizationId(id));
    }

    @NonNull
    @PreAuthorize("hasRole('SUPER_USER') or hasRole('POWER_USER')")
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

        return surveyRespondentsFacade.getSurveyRespondents(new SurveyId(surveyId),
                respondentFilterAssembler.fromInput(filters),
                resolverHelper.getLanguageCode(environment));
    }
}
