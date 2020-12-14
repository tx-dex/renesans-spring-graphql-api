package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.RespondentFilterAssembler;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.facade.SurveyRespondentsFacade;
import fi.sangre.renesans.graphql.input.*;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.graphql.input.question.QuestionDriverWeightInput;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.UserService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class AdminMutations implements GraphQLMutationResolver {
    private final OrganizationService organizationService;
    private final OrganizationSurveyService organizationSurveyService;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final SurveyRespondentsFacade surveyRespondentsFacade;
    private final RespondentFilterAssembler respondentFilterAssembler;
    private final ResolverHelper resolverHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;

    public AuthorizationOutput login(@NotNull String username, @NotNull String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        final String jwt = jwtTokenService.generateToken(authentication);

        return AuthorizationOutput.builder()
                .token(jwt)
                .build();
    }

    // TODO refine
    @PreAuthorize("isAuthenticated()")
    public Boolean logout(String token) {
        // TODO handle token invalidation immediately
        return true;
    }

    @PreAuthorize("hasRole('SUPER_USER') or (hasRole('POWER_USER') and authentication.principal.id == #id)")
    public User storeUser(
            @P("id") Long id,
            String firstName,
            String lastName,
            String email,
            String password,
            String username,
            Boolean enabled,
            List<String> roles,
            DataFetchingEnvironment environment
    ) {
        final String locale = resolverHelper.getLanguageCode(environment);
        return userService.storeUser(id, firstName, lastName, email, password, username, enabled, roles, locale);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public User removeUser(@P("id") Long id) {
        return userService.removeUser(id);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public User allowUserCustomerAccess(
            Long id,
            Long customerId
    ) {
        return userService.setUserAccess(id, customerId, true);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public User revokeUserCustomerAccess(
            Long id,
            Long customerId
    ) {
        return userService.setUserAccess(id, customerId, false);
    }

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

        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromParametersInput(id, version, input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.updateMetadata(inputSurvey);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyStaticText(@NonNull final UUID id,
                                                                @NonNull final Long version,
                                                                @NonNull final StaticTextInput input,
                                                                @Nullable final String languageCode,
                                                                @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromStaticTextInput(id, version, input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.updateMetadata(inputSurvey);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyCatalysts(@NonNull final UUID id,
                                                               @NonNull final Long version,
                                                               @NonNull final List<CatalystInput> input,
                                                               @Nullable final String languageCode,
                                                               @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromCatalystAndDriversInput(id, version, input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.updateMetadata(inputSurvey);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyQuestions(@NonNull final UUID id,
                                                               @NonNull final Long version,
                                                               @NonNull final List<CatalystInput> input,
                                                               @Nullable final String languageCode,
                                                               @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromQuestionsInput(id, version, input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.updateMetadata(inputSurvey);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyQuestionWeight(@NonNull final UUID id,
                                                                   @NonNull final Long version,
                                                                   @NonNull final QuestionDriverWeightInput input,
                                                                   @Nullable final String languageCode,
                                                                   @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyService.updateQuestionDriverWeights(new SurveyId(id), version, input);
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'INVITE')")
    public Collection<RespondentOutput> inviteRespondents(@NonNull final UUID surveyId,
                                                          @NonNull final RespondentInvitationInput invitation,
                                                          @Nullable final List<FilterInput> filters,
                                                          @NonNull final String languageCode,
                                                          @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);
        final UserDetails principal = resolverHelper.getRequiredPrincipal(environment);

        if (principal instanceof UserPrincipal) {
            return surveyRespondentsFacade.inviteRespondents(new SurveyId(surveyId),
                    invitation,
                    respondentFilterAssembler.fromInput(filters),
                    resolverHelper.getLanguageCode(environment),
                    (UserPrincipal ) principal);
        } else {
            throw new SurveyException("Only user can invite respondents");
        }
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'INVITE')")
    public RespondentOutput removeRespondent(@NonNull final UUID surveyId, @NonNull final UUID id) {
        return surveyRespondentsFacade.softDeleteRespondent(new RespondentId(id));
    }

}