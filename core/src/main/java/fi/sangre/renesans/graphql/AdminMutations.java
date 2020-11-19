package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.graphql.input.OrganizationInput;
import fi.sangre.renesans.graphql.input.SurveyInput;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminMutations implements GraphQLMutationResolver {
    private final OrganizationService organizationService;
    private final OrganizationSurveyService organizationSurveyService;
    private final ResolverHelper resolverHelper;

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public Organization storeOrganization(@NonNull final OrganizationInput input) {
        return organizationService.storeOrganization(input);
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public Organization removeOrganization(@NonNull final UUID id) {
        return organizationService.softDeleteOrganization(id);
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public OrganizationSurvey storeOrganizationSurvey(@NonNull final UUID organizationId,
                                                      @NonNull final SurveyInput input,
                                                      @Nullable final String languageCode,
                                                      @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationService.storeSurvey(organizationId, input, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public OrganizationSurvey storeOrganizationSurveyParameters(@NonNull final UUID id,
                                                                @NonNull final Long version,
                                                                @NonNull final List<SurveyParameterInput> input,
                                                                @Nullable final String languageCode,
                                                                @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyService.storeSurveyParameters(id, version, input, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public OrganizationSurvey removeOrganizationSurvey(@NonNull final UUID id) {
        return organizationSurveyService.softDeleteSurvey(id);
    }
}
