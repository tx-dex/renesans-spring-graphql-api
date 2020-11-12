package fi.sangre.renesans.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.dto.*;
import fi.sangre.renesans.exception.DeprecatedException;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.service.*;
import fi.sangre.renesans.statistics.ComparativeStatistics;
import fi.sangre.renesans.statistics.Statistics;
import graphql.GraphQLException;
import graphql.language.Field;
import graphql.language.Selection;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor

@Component
@Slf4j
public class Query implements GraphQLQueryResolver {
    private final RespondentGroupService respondentGroupService;
    private final SurveyService surveyService;
    private final RespondentOptionService respondentOptionService;
    private final MultilingualService multilingualService;
    private final InvitationService invitationService;
    private final CustomerService customerService;
    private final RespondentService respondentService;
    private final StatisticsService statisticsService;
    private final ImageUploadService imageUploadService;
    private final UserService userService;
    private final RoleService roleService;
    private final JwtTokenService tokenService;
    private final SegmentService segmentService;
    private final QuestionnaireService questionnaireService;
    private final QuestionService questionService;
    private final ResolverHelper resolverHelper;

    @PreAuthorize("isAuthenticated()")
    public List<Customer> customers(String languageCode, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return customerService.getAllCustomers();
    }

    @PreAuthorize("isAuthenticated()")
    public Segment segment(String languageCode, Long id, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return segmentService.getSegmentById(id);
    }

    @PreAuthorize("isAuthenticated()")
    public List<Segment> segments(DataFetchingEnvironment environment) {
        return segmentService.getAllSegments();
    }

    @PreAuthorize("isAuthenticated()")
    public Customer customer(String languageCode, Long id, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return customerService.getCustomer(id);
    }

    @PreAuthorize("isAuthenticated()")
    public Question question(String languageCode, Long id, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionService.getQuestion(id);
    }

    //TODO: create survey dto
    // INTERNAL QUERY - THE PUBLIC USES Query::questionnaire
    @PreAuthorize("isAuthenticated()")
    public Survey survey(final String languageCode, final String id, final Long customerId, final DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return surveyService.getSurvey(id, customerId);
    }

    @PreAuthorize("isAuthenticated()")
    public Iterable<Respondent> respondents(
            FiltersDto filters,
            DataFetchingEnvironment environment
    ) {
        if (filters == null) {
            filters = new FiltersDto();
        }

        // TODO currently using default survey always. Change when necessary (ie. iCan or something like that)
        filters.setSurveyId(surveyService.getDefaultSurvey().getId());

        return respondentService.getUniqueRespondents(filters);
    }

    public Statistics statistics(
            final String languageCode,
            final FiltersDto filters,
            final DataFetchingEnvironment environment
    ) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return statisticsService.statistics(filters, resolverHelper.getLanguageCode(environment));
    }

    @PreAuthorize("isAuthenticated()")
    public ComparativeStatistics comparativeStatistics(
            String languageCode,
            FiltersDto filters,
            List<Long> customerIds,
            List<String> respondentGroupIds,
            List<String> respondentIds,
            Boolean edit,
            DataFetchingEnvironment environment
    ) {
        log.debug("Input arguments: languageCode={}, filters={}, customerIds={}, respondentGroupIds={}, respondentIds={},environment={}", languageCode, filters, customerIds, respondentGroupIds, respondentIds, environment);

        resolverHelper.setLanguageCode(languageCode, environment);

        return statisticsService.comparativeStatistics(filters, customerIds, respondentGroupIds, respondentIds, edit != null && edit, resolverHelper.getLanguageCode(environment));
    }

    public List<RespondentOption> respondentOptions(String languageCode, RespondentOption.OptionType optionType,
                                                    DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return respondentOptionService.getRespondentOptions(optionType);
    }

    public List<Country> countries(String languageCode, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return multilingualService.getCountries(resolverHelper.getLanguageCode(environment));
    }

    public MultilingualPhrase phrase(String languageCode, String key, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return multilingualService.getPhrase(key, resolverHelper.getLanguageCode(environment));
    }

    /**
     * TODO write the locale query out
     * The locale query is provided only because removing it would require significant refactoring in the survey front end
     * Eventually should be replaced with phrases endpoint
     */
    public LocaleDto locale(String languageCode, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        Field baseField = environment.getFields().get(0);
        List<Selection> selections = baseField.getSelectionSet().getSelections();
        List<String> keys = selections.stream().map(selection -> ((Field) selection).getName()).collect(Collectors.toList());
        Map<String, String> phrases = multilingualService.lookupPhrases(keys, "", resolverHelper.getLanguageCode(environment));
        return new LocaleDto(phrases);
    }

    public List<MultilingualPhrase> phrases(String languageCode,
                                            List<String> keys,
                                            String startsWith,
                                            DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return multilingualService.getPhrases(keys, startsWith, resolverHelper.getLanguageCode(environment));
    }

    @Deprecated
    public String defaultRespondentGroupId(DataFetchingEnvironment environment) {
        throw new DeprecatedException();
    }

    @PreAuthorize("isAuthenticated()")
    public List<InvitationDetailsDto> invitations(String respondentGroupId) {
        return invitationService.getInvitationsByRespondentGroup(respondentGroupId);
    }

    @PreAuthorize("isAuthenticated()")
    public List<InvitationDetailsDto> invitationsByEmail(String respondentGroupId, String email, String status) {
        return invitationService.getInvitationsByEmail(respondentGroupId, email, status);
    }

    public String defaultSurveyId() {
        return surveyService.getDefaultSurvey().getId();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public RespondentGroup respondentGroup(String languageCode, String id, DataFetchingEnvironment environment) {
        throw new DeprecatedException();
    }

    @Deprecated
    @PreAuthorize("isAuthenticated()")
    public List<RespondentGroup> respondentGroups(String languageCode, Long customerId, DataFetchingEnvironment environment) {
        return ImmutableList.of();
    }

    public List<Language> languages(String languageCode, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return multilingualService.getLanguages(resolverHelper.getLanguageCode(environment));
    }

    @PreAuthorize("isAuthenticated()")
    public ImageUploadUrlDto imageUploadUrl(String fileName) {
        return imageUploadService.getUploadUrl(fileName);
    }

    // PUBLIC QUERY FOR SURVEY
    public QuestionnaireDto questionnaire(String languageCode, String respondentGroupId, String respondentId, DataFetchingEnvironment environment) {
        resolverHelper.setLanguageCode(languageCode, environment);

        return questionnaireService.getQuestionnaire(respondentGroupId, respondentId);
    }

    @PreAuthorize("isAuthenticated() and (hasRole('SUPER_USER') or (#id == null or #id == authentication.principal.id))")
    public User user(Long id) {
        return id == null ? userService.findLoggedInUser() : userService.findById(id);
    }

    @PreAuthorize("hasRole('SUPER_USER')")
    public List<User> users() {
        return userService.findUsers();
    }

    @PreAuthorize("isAuthenticated()")
    public List<Role> roles() {
        return roleService.findAll();
    }

    public boolean validateResetPasswordToken(String token) {
        try {
            return tokenService.validateResetPasswordToken(token);
        } catch (final Exception e) {
            log.warn("Invalid reset password token", e);
            throw new GraphQLException("Invalid reset password token", e);
        }
    }

    // only super users for now as no other roles are allowed to user management
    @PreAuthorize("isAuthenticated()")
    public ValidationDto validateEmail(String value, Long userId) {
        Boolean valid = !userService.isEmailRegistered(value, userId);
        String error = !valid
                ? "Email address has already been registered."
                : null;

        return new ValidationDto(valid, error);
    }

    // only super users for now as no other roles are allowed to user management, and
    // users can't change their own user names
    @PreAuthorize("hasRole('SUPER_USER')")
    public ValidationDto validateUsername(String value, Long userId) {
        Boolean valid = !userService.isUsernameRegistered(value, userId);
        String error = !valid
                ? "Username has already been registered."
                : null;

        return new ValidationDto(valid, error);
    }

    @Deprecated
    public RespondentGroup defaultRespondentGroup(String languageCode, DataFetchingEnvironment environment) {
        throw new DeprecatedException();
    }
}
