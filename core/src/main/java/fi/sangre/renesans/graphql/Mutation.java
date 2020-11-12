package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.dto.AuthorizationDto;
import fi.sangre.renesans.dto.ResultDetailsDto;
import fi.sangre.renesans.exception.DeprecatedException;
import fi.sangre.renesans.graphql.input.*;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.service.*;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

@RequiredArgsConstructor

@Slf4j
@Component
public class Mutation implements GraphQLMutationResolver {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService tokenService;
    private final CustomerService customerService;
    private final WeightService weightService;
    private final InvitationService invitationService;
    private final MultilingualService multilingualService;
    private final RespondentService respondentService;
    private final UserService userService;
    private final SegmentService segmentService;
    private final QuestionService questionService;
    private final OrganizationService organizationService;
    private final ResolverHelper resolverHelper;

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

    @PreAuthorize("isAuthenticated() and authentication.principal.id == #id") // whatever role user has it can update it's password
    public boolean updatePassword(final Long id, final String oldPassword, final String newPassword) {
        checkArgument(id != null, "UserId is required");
        checkArgument(!isNullOrEmpty(oldPassword), "OldPassword is required");
        checkArgument(!isNullOrEmpty(newPassword), "NewPassword is required");

        userService.updatePassword(id, oldPassword, newPassword);

        return true;
    }

    @PreAuthorize("hasRole('SUPER_USER')") // whatever role user has it can update it's password
    public boolean adminUpdatePassword(final Long id, final String newPassword) {
        checkArgument(id != null, "UserId is required");
        checkArgument(!isNullOrEmpty(newPassword), "NewPassword is required");

        userService.updatePassword(id, newPassword);

        return true;
    }

    public boolean requestPasswordReset(String email, DataFetchingEnvironment environment) {
        try {
            final String locale = resolverHelper.getLanguageCode(environment);
            userService.requestPasswordReset(email, locale);
            return true;
        } catch (final Exception e) {
            log.warn("Cannot request password reset for email: {}", email, e);
            throw new GraphQLException("Internal error");
        }
    }

    public boolean resetUserPassword(final String token, final String newPassword) {
        checkArgument(!isNullOrEmpty(newPassword), "NewPassword is required");

        try {
            userService.resetPassword(token, newPassword);
            return true;
        } catch (final Exception e) {
            log.warn("Cannot reset user password", e);
            throw new GraphQLException("Cannot reset user password", e);
        }
    }

    @PreAuthorize("isAuthenticated()")
    public Segment storeSegment(final String languageCode, final SegmentInput segmentInput, final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return segmentService.storeSegment(languageCode, segmentInput);
    }

    @PreAuthorize("isAuthenticated()")
    public Question storeCustomerQuestion(final String languageCode,final Long customerId, final QuestionInput question, final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionService.storeCustomerQuestion(languageCode, customerId, question);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public Question storeSegmentQuestion(final String languageCode, final Long segmentId, final QuestionInput question, final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionService.storeSegmentQuestion(languageCode, segmentId, question);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public Question storeQuestion(final String languageCode, final QuestionInput question, final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionService.storeQuestion(languageCode, question);
    }

    @PreAuthorize("isAuthenticated()")
    public Question removeQuestion(final Long id, final DataFetchingEnvironment environment) {
        return questionService.removeQuestion(id);
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public RespondentGroup storeRespondentGroup(RespondentGroupInput respondentGroupInput) {
        throw new DeprecatedException();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public Respondent moveRespondentToRespondentGroup(String respondentId, String respondentGroupId) {
        throw new DeprecatedException();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public Respondent copyRespondentToRespondentGroup(String respondentId, String respondentGroupId) {
        throw new DeprecatedException();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public RespondentGroup removeRespondentGroup(String id) {
        throw new DeprecatedException();
    }

    // TODO refine
    @PreAuthorize("isAuthenticated()")
    public Respondent removeRespondent(String id) {
        return respondentService.removeRespondent(id);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public MultilingualPhrase storeNamedPhrase(PhraseInput phraseInput, DataFetchingEnvironment environment) {
        // if input does not have a set languageCode, set it from the request context
        if (phraseInput.getLanguageCode() == null) {
            phraseInput.setLanguageCode(resolverHelper.getLanguageCode(environment));
        }
        return multilingualService.saveNamedPhrase(phraseInput);
    }

    @PreAuthorize("isAuthenticated()")
    public List<Weight> storeWeights(List<WeightInput> weights) {
        return weightService.storeWeights(weights);
    }

    // TODO refine
    @PreAuthorize("isAuthenticated()")
    public ResultDetailsDto sendInvitation(InvitationInput invitation, List<RecipientInput> recipients) {
        return invitationService.sendInvitation(invitation, recipients);
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public Customer storeCustomer(@NonNull final CustomerInput customer) {
        final Organization organization = organizationService.storeOrganization(OrganizationInput.builder()
                .id(customer.getId())
                .name(customer.getName())
                .description(customer.getDescription())
                .segmentId(customer.getSegmentId())
                .build());
        return Customer.builder()
                .id(organization.getId())
                .name(organization.getName())
                .description(organization.getDescription())
                .build();
    }

    @NonNull
    @PreAuthorize("isAuthenticated()")
    public Organization storeOrganization(@NonNull final OrganizationInput input) {
        return organizationService.storeOrganization(input);
    }

    @PreAuthorize("isAuthenticated()")
    public Customer storeCustomerDriverWeights(Long customerId, List<DriverWeightInput> driverWeightInput ) {
        return customerService.storeCustomerDriverWeights(customerId, driverWeightInput);
    }

    // TODO refine
    @PreAuthorize("isAuthenticated()")
    public Customer removeCustomer(Long id) {
        return customerService.deleteCustomer(id);
    }

    @Deprecated
    public Respondent submitSurvey(String respondentGroupId, RespondentInput respondentInput, List<AnswerInput> answers) {
        throw new DeprecatedException();
    }

    public AuthorizationDto login(@NotNull String username, @NotNull String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        username,
                        password
                )
        );

        final String jwt = tokenService.generateToken(authentication);

        return AuthorizationDto.builder()
                .token(jwt)
                .build();
    }

    // TODO refine
    @PreAuthorize("isAuthenticated()")
    public Boolean logout(String token) {
        // TODO handle token invalidation immediately
        return true;
    }
}
