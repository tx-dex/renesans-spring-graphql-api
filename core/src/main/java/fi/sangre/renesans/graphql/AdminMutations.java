package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.RespondentFilterAssembler;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.assemble.OrganizationOutputAssembler;
import fi.sangre.renesans.graphql.assemble.aaa.UserOutputAssembler;
import fi.sangre.renesans.graphql.facade.aftergame.AfterGameFacade;
import fi.sangre.renesans.graphql.facade.OrganizationSurveyFacade;
import fi.sangre.renesans.graphql.facade.SurveyRespondentsFacade;
import fi.sangre.renesans.graphql.facade.aftergame.DialogueFacade;
import fi.sangre.renesans.graphql.input.*;
import fi.sangre.renesans.graphql.input.dialogue.DialogueTopicInput;
import fi.sangre.renesans.graphql.input.discussion.DiscussionQuestionInput;
import fi.sangre.renesans.graphql.input.media.MediaDetailsInput;
import fi.sangre.renesans.graphql.input.media.MediaUploadInput;
import fi.sangre.renesans.graphql.input.media.SurveyMediaInput;
import fi.sangre.renesans.graphql.input.parameter.SurveyParameterInput;
import fi.sangre.renesans.graphql.input.question.QuestionDriverWeightInput;
import fi.sangre.renesans.graphql.output.AuthorizationOutput;
import fi.sangre.renesans.graphql.output.OrganizationOutput;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import fi.sangre.renesans.graphql.output.aaa.UserOutput;
import fi.sangre.renesans.graphql.output.dialogue.DialogueTopicOutput;
import fi.sangre.renesans.graphql.output.media.MediaUploadOutput;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.service.MediaService;
import fi.sangre.renesans.service.OrganizationService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.UserService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
    private final OrganizationSurveyFacade organizationSurveyFacade;
    private final DialogueFacade dialogueFacade;
    private final OrganizationOutputAssembler organizationOutputAssembler;
    private final OrganizationSurveyService organizationSurveyService;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final SurveyRespondentsFacade surveyRespondentsFacade;
    private final AfterGameFacade afterGameFacade;
    private final RespondentFilterAssembler respondentFilterAssembler;
    private final ResolverHelper resolverHelper;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;
    private final UserService userService;
    private final UserOutputAssembler userOutputAssembler;
    private final MediaService mediaService;

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

    @NonNull
    @PreAuthorize("hasRole('SUPER_USER') or (hasRole('POWER_USER') and authentication.principal.id == #id)")
    public UserOutput storeUser(
            @Nullable final Long id,
            @Nullable final String firstName,
            @Nullable final String lastName,
            @Nullable final String email,
            @Nullable final String password,
            @Nullable final String username,
            @Nullable final Boolean enabled,
            @Nullable final List<String> roles,
            @NonNull final DataFetchingEnvironment environment
    ) {
        final String locale = resolverHelper.getLanguageCode(environment);
        return userOutputAssembler.from(userService.storeUser(
                id,
                firstName,
                lastName,
                email,
                username,
                enabled,
                roles,
                (UserPrincipal) resolverHelper.getRequiredPrincipal(environment),
                locale));
    }

    @NonNull
    @PreAuthorize("hasRole('SUPER_USER')")
    public UserOutput removeUser(@NonNull final Long id) {
        return userOutputAssembler.from(userService.removeUser(id));
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public UserOutput allowUserCustomerAccess(@NonNull final Long id, @NonNull final UUID customerId) {
        return userOutputAssembler.from(userService.setUserAccess(
                id
                , new OrganizationId(customerId),
                true));
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public UserOutput revokeUserCustomerAccess(@NonNull final Long id, @NonNull final UUID customerId) {
        return userOutputAssembler.from(userService.setUserAccess(
                id,
                new OrganizationId(customerId),
                false));
    }

    @NonNull
    @PreAuthorize("isAuthenticated()") //TODO: implement it properly
    public OrganizationOutput storeOrganization(@NonNull final OrganizationInput input) {
        return organizationOutputAssembler.from(organizationService.storeOrganization(input)); //TODO: move to facade
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'organization', 'DELETE')")
    public OrganizationOutput removeOrganization(@NonNull final UUID id) {
        return organizationOutputAssembler.from(organizationService.softDeleteOrganization(id));
    }

    @NonNull
    @PreAuthorize("hasPermission(#organizationId, 'organization', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurvey(@NonNull final UUID organizationId,
                                                      @NonNull final SurveyInput input,
                                                      @Nullable final String languageCode,
                                                      @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyService.storeSurvey(new OrganizationId(organizationId), input, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#organizationId, 'organization', 'WRITE')")
    public OrganizationSurvey copyOrganizationSurvey(@NonNull final UUID organizationId,
                                                     @NonNull final SurveyInput input,
                                                     @Nullable final String languageCode,
                                                     @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyFacade.copySurvey(new OrganizationId(organizationId), input, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey enableAfterGame(@NonNull final UUID id,
                                              @NonNull final Long version,
                                              @Nullable final String languageCode,
                                              @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return organizationSurveyFacade.enableAfterGame(new SurveyId(id), version);
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
    public OrganizationSurvey storeOrganizationSurveyDiscussionQuestions(@NonNull final UUID id,
                                                                         @NonNull final Long version,
                                                                         @NonNull final List<DiscussionQuestionInput> input,
                                                                         @Nullable final String languageCode,
                                                                         @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromDiscussionQuestionInput(id, version, input, resolverHelper.getLanguageCode(environment));
        return organizationSurveyService.updateMetadata(inputSurvey);
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyLogo(@NonNull final UUID id,
                                                          @NonNull final Long version,
                                                          @NonNull MediaDetailsInput details) {
        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromLogoInput(id, version, details);
        return organizationSurveyService.updateMetadata(inputSurvey);
    }


    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public OrganizationSurvey storeOrganizationSurveyMedia(@NonNull final UUID id,
                                                           @NonNull final Long version,
                                                           @NonNull final List<SurveyMediaInput> input,
                                                           @Nullable final String languageCode,
                                                           @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        final OrganizationSurvey inputSurvey = organizationSurveyAssembler
                .fromMediasInput(id, version, input, resolverHelper.getLanguageCode(environment));
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
    public OrganizationSurvey inviteToAfterGame(@NonNull final UUID surveyId,
                                                @NonNull final MailInvitationInput invitation,
                                                @NonNull final String languageCode,
                                                @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.inviteToAfterGame(new SurveyId(surveyId), invitation, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'INVITE')")
    public OrganizationSurvey inviteToAfterGameDiscussion(@NonNull final UUID surveyId,
                                                          @NonNull final UUID questionId,
                                                          @NonNull final MailInvitationInput invitation,
                                                          @NonNull final String languageCode,
                                                          @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return afterGameFacade.inviteToAfterGameDiscussion(new SurveyId(surveyId), new QuestionId(questionId), invitation, resolverHelper.getRequiredPrincipal(environment));
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'INVITE')")
    public RespondentOutput removeRespondent(@NonNull final UUID surveyId, @NonNull final UUID id) {
        return surveyRespondentsFacade.softDeleteRespondent(new RespondentId(id));
    }

    @NonNull
    @PreAuthorize("hasPermission(#id, 'survey', 'WRITE')")
    public MediaUploadOutput getMediaUploadUrl(@NonNull final UUID id,
                                               @NonNull final MediaUploadInput input) {
        return mediaService.requestUploadUrl(new SurveyId(id), input);
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'WRITE')")
    public Collection<DialogueTopicOutput> storeDialogueTopics(@NonNull final Collection<DialogueTopicInput> inputs,
                                                  @NonNull final UUID surveyId,
                                                  @Nullable final String languageCode,
                                                  @NonNull final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);
        return dialogueFacade.storeTopics(surveyId, inputs);
    }

    @NonNull
    @PreAuthorize("hasPermission(#surveyId, 'survey', 'WRITE')")
    public boolean changeSurveyDialogueActivation(@NonNull UUID surveyId, @NonNull Boolean isActive) {
        return dialogueFacade.changeSurveyDialogueActivation(surveyId, isActive);
    }
}