package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import fi.sangre.renesans.exception.DeprecatedException;
import fi.sangre.renesans.graphql.input.*;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.service.QuestionService;
import fi.sangre.renesans.service.SegmentService;
import fi.sangre.renesans.service.UserService;
import graphql.GraphQLException;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

@RequiredArgsConstructor

@Slf4j
@Component
@Deprecated
public class Mutation implements GraphQLMutationResolver {
    private final UserService userService;
    private final SegmentService segmentService;
    private final QuestionService questionService;
    private final ResolverHelper resolverHelper;

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
    public Question storeCustomerQuestion(final String languageCode,final UUID customerId, final QuestionInput question, final DataFetchingEnvironment environment) {
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

    @Deprecated
    @PreAuthorize("hasRole('SUPER_USER')")
    public MultilingualPhrase storeNamedPhrase(PhraseInput phraseInput, DataFetchingEnvironment environment) {
        throw new DeprecatedException();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public List<Weight> storeWeights(List<WeightInput> weights) {
        throw new DeprecatedException();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public Customer storeCustomerDriverWeights(UUID customerId, List<DriverWeightInput> driverWeightInput ) {
        throw new DeprecatedException();
    }

    @Deprecated
    public Respondent submitSurvey(String respondentGroupId, RespondentInput respondentInput, List<AnswerInput> answers) {
        throw new DeprecatedException();
    }
}
