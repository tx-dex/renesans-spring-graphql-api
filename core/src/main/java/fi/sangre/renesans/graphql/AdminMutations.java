package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.application.assemble.CatalystAssembler;
import fi.sangre.renesans.application.assemble.ParameterAssembler;
import fi.sangre.renesans.application.assemble.StaticTextAssembler;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.StaticText;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.graphql.facade.SurveyRespondentsFacade;
import fi.sangre.renesans.graphql.input.*;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminMutations implements GraphQLMutationResolver {
    private final OrganizationService organizationService;
    private final OrganizationSurveyService organizationSurveyService;
    private final SurveyRespondentsFacade surveyRespondentsFacade;
    private final ParameterAssembler parameterAssembler;
    private final StaticTextAssembler staticTextAssembler;
    private final CatalystAssembler catalystAssembler;
    private final ResolverHelper resolverHelper;

    @NonNull
    @PreAuthorize("isAuthenticated()") //TODO: implement it properly
    public Organization storeOrganization(@NonNull final OrganizationInput input) {
        return organizationService.storeOrganization(input);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'organization', 'DELETE')")
    public Organization removeOrganization(@NonNull final UUID id) {
        return organizationService.softDeleteOrganization(id);
    }

    @NonNull
    @PreAuthorize("hasPermission(#organizationId, 'organization', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurvey(@NonNull final UUID organizationId,
                                                      @NonNull final SurveyInput input,
                                                      @Nullable final String languageCode,
                                                      @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyService.storeSurvey(organizationId, input, resolverHelper.getLanguageCode(environment));
    }


    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'DELETE')")
    public OrganizationSurvey removeOrganizationSurvey(@NonNull final UUID id) {
        return organizationSurveyService.softDeleteSurvey(id);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyParameters(@NonNull final UUID id,
                                                                @NonNull final Long version,
                                                                @NonNull final List<SurveyParameterInput> input,
                                                                @Nullable final String languageCode,
                                                                @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final List<Parameter> parameters = parameterAssembler.fromInputs(input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.storeSurveyParameters(id, version, parameters);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyStaticText(@NonNull final UUID id,
                                                                @NonNull final Long version,
                                                                @NonNull final StaticTextInput input,
                                                                @Nullable final String languageCode,
                                                                @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final StaticText text = staticTextAssembler.from(input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.storeSurveyStaticText(id, version, text);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyCatalysts(@NonNull final UUID id,
                                                               @NonNull final Long version,
                                                               @NonNull final List<CatalystInput> input,
                                                               @Nullable final String languageCode,
                                                               @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final List<Catalyst> catalysts = catalystAssembler.fromInput(input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.storeSurveyCatalysts(id, version, catalysts);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyQuestions(@NonNull final UUID id,
                                                               @NonNull final Long version,
                                                               @NonNull final List<CatalystInput> input,
                                                               @Nullable final String languageCode,
                                                               @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final List<Catalyst> catalysts = catalystAssembler.fromInput(input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.storeSurveyQuestions(id, version, catalysts);
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'INVITE')")
    public Collection<RespondentOutput> inviteRespondents(@NonNull final UUID surveyId,
                                                          @NonNull final List<RespondentInvitationInput> invitations,
                                                          @Nullable final List<FilterInput> filters,
                                                          @NonNull final String languageCode,
                                                          @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return surveyRespondentsFacade.inviteRespondents(surveyId, invitations, filters, resolverHelper.getLanguageCode(environment));
    }
}