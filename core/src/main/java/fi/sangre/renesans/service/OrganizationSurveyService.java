package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.RespondentAssembler;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.event.InviteToQuestionnaireEvent;
import fi.sangre.renesans.application.event.QuestionnaireOpenedEvent;
import fi.sangre.renesans.application.merge.OrganizationSurveyMerger;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.SurveyInput;
import fi.sangre.renesans.graphql.input.question.QuestionDriverWeightInput;
import fi.sangre.renesans.persistence.assemble.SurveyAssembler;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.SurveyRespondent;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import fi.sangre.renesans.persistence.model.metadata.LocalisationMetadata;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static fi.sangre.renesans.config.ApplicationConfig.ASYNC_EXECUTOR_NAME;
import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.*;


@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationSurveyService {
    private final SurveyRepository surveyRepository;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final OrganizationSurveyMerger organizationSurveyMerger;
    private final RespondentDao respondentDao;
    private final SurveyDao surveyDao;
    private final SurveyUtils surveyUtils;
    private final CustomerRepository customerRepository;
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final RespondentAssembler respondentAssembler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MultilingualUtils multilingualUtils;
    private final SurveyAssembler surveyAssembler;
    private final SurveyTemplateService surveyTemplateService;
    private final TranslationService translationService;

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey getSurvey(@NonNull final SurveyId id) {
        return organizationSurveyAssembler.from(getSurveyOrThrow(id.getValue()));
    }

    @NonNull
    @Transactional(readOnly = true)
    public OrganizationSurvey getSurvey(@NonNull final UUID id) {
        return organizationSurveyAssembler.from(getSurveyOrThrow(id));
    }

    @NonNull
    @Transactional(readOnly = true)
//    @PostFilter("hasPermission(filterObject, 'READ')")
    public List<OrganizationSurvey> getSurveys(@NonNull final UUID organizationId, @NonNull final String languageTag) {
        return customerRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"))
                .getSurveys().stream()
                .map(organizationSurveyAssembler::from)
                .sorted((e1,e2) -> compare(e1.getTitles().getPhrases(), e2.getTitles().getPhrases(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    @Transactional
    public OrganizationSurvey copySurvey(@NonNull final OrganizationId targetId,
                                         @NonNull final SurveyId sourceId,
                                         @NonNull final MultilingualText titles,
                                         @NonNull final MultilingualText descriptions,
                                         final List<String> languages) {
        final Survey survey = surveyRepository.saveAndFlush(createCopy(sourceId, titles, descriptions, languages));

        addToOrganization(targetId, survey);

        return organizationSurveyAssembler.from(survey);
    }

    @NonNull
    @Transactional
    public OrganizationSurvey storeSurvey(@NonNull final OrganizationId organizationId, @NonNull final SurveyInput input, @NonNull final String languageTag) {

        final Survey survey;
        if (input.getId() == null) {
            final MultilingualText titles = multilingualUtils.create(input.getTitle(), languageTag);
            final MultilingualText descriptions = multilingualUtils.create(input.getDescription(), languageTag);
            final SurveyId sourceId = Optional.ofNullable(input.getSourceSurveyId())
                    .map(SurveyId::new)
                    .orElse(null);

            survey = createOrCopySurvey(sourceId, titles, descriptions, input.getLanguages());

            addToOrganization(organizationId, survey);
        } else { // update
            checkArgument(input.getVersion() != null, "input.version cannot be null");

            survey = surveyRepository.findById(input.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));
            if (!input.getVersion().equals(survey.getVersion())) {
                throw new SurveyException("Invalid survey version");
            }

            final SurveyMetadata metadata = copyMetadata(survey.getMetadata())
                    .build();

            if (input.getProperties() != null) { // update only properties
                Optional.ofNullable(input.getProperties().getHideCatalystThemePages())
                        .ifPresent(metadata::setHideCatalystThemePages);

            } else {
                final MultilingualText titles = multilingualUtils.combine(
                        multilingualUtils.create(metadata.getTitles()),
                        multilingualUtils.create(input.getTitle(), languageTag));
                final MultilingualText descriptions = multilingualUtils.combine(
                        multilingualUtils.create(metadata.getDescriptions()),
                        multilingualUtils.create(input.getDescription(), languageTag));

                if (titles.isEmpty()) {
                    throw new SurveyException("Title must not be empty");
                }

                metadata.setTitles(titles.getPhrases());
                metadata.setDescriptions(descriptions.getPhrases());

                if(input.getLanguages() != null) {
                    survey.setLanguages(input.getLanguages());
                }
            }

            survey.setMetadata(metadata);

            surveyRepository.saveAndFlush(survey);
        }

        return organizationSurveyAssembler.from(survey);
    }

    @NonNull
    private Survey createCopy(@NonNull final SurveyId sourceId,
                              @NonNull final MultilingualText titles,
                              @NonNull final MultilingualText descriptions,
                              final List<String> languages) {
        final OrganizationSurvey source = getSurvey(sourceId);

        final MultilingualText newTitles;
        if (titles.isEmpty()) {
            newTitles = multilingualUtils.create(source.getTitles().getPhrases().entrySet().stream()
                    .filter(e -> Objects.nonNull(e.getValue()))
                    .map(e -> Pair.of(e.getKey(), "Copy of " + e.getValue()))
                    .collect(toMap(Pair::getLeft, Pair::getRight)));
        } else {
            newTitles = multilingualUtils.combine(source.getTitles(), titles);
        }

        source.setId(null);
        source.setVersion(1L);
        source.setTitles(newTitles);
        source.setDescriptions(multilingualUtils.combine(source.getDescriptions(), descriptions));
        source.setLanguages(languages);

        final Survey copy = surveyAssembler.from(source);
        copy.setState(SurveyState.OPEN);

        return copy;
    }

    private void addToOrganization(@NonNull final OrganizationId id, @NonNull final Survey survey) {
        final Customer customer = customerRepository.findById(id.getValue())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        customer.getSurveys().add(survey);

        customerRepository.save(customer);
    }

    @NonNull
    public OrganizationSurvey updateQuestionDriverWeights(@NonNull final SurveyId surveyId, @NonNull final Long version, @NonNull final QuestionDriverWeightInput input) {
        checkArgument(input.getWeight() >= 0, "Weight must have positive value");

        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(surveyId);

        survey.setVersion(version);
        final QuestionId questionId = new QuestionId(input.getQuestionId());
        final LikertQuestion question = Objects.requireNonNull(surveyUtils.findQuestion(questionId, survey),
                "Survey likert question not found");
        //TODO: validate that driver id belongs to survey
        final Map<DriverId, Double> weights = new LinkedHashMap<>(question.getWeights());
        weights.put(new DriverId(input.getDriverId()), input.getWeight());
        question.setWeights(weights);

        return updateMetadata(survey);
    }

    @NonNull
    public OrganizationSurvey updateMetadata(@NonNull final OrganizationSurvey input) {
        final OrganizationSurvey existing = surveyDao.getSurveyOrThrow(new SurveyId(input.getId()));
        final OrganizationSurvey updated = organizationSurveyMerger.combine(existing, input);

        try {
            return surveyDao.store(updated);
        } catch (final ObjectOptimisticLockingFailureException ex) {
            log.warn("Survey was updated already by someone else.", ex);
            throw new SurveyException("Survey was updated already by someone else");
        }
    }

    @Transactional
    public void inviteRespondents(@NonNull final SurveyId surveyId
            , @NonNull final Invitation invitation
            , @NonNull final Pair<String, String> replyTo) {
        final String languageTag = translationService.getLanguageTagOrDefault(invitation.getLanguage());
        final Survey survey = getSurveyOrThrow(surveyId);

        final Map<String, SurveyRespondent> existing = surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()).stream()
                .filter(e -> invitation.getEmails().contains(e.getEmail()))
                .collect(toMap(SurveyRespondent::getEmail, e -> e));

        final List<SurveyRespondent> respondents = invitation.getEmails().stream()
                .map(e -> existing.getOrDefault(e, SurveyRespondent.builder()
                        .surveyId(surveyId.getValue())
                        // generate a random color for each new respondent to show it later in the comments
                        .color(String.format("#%06x", (new Random()).nextInt(0x1000000)))
                        .email(StringUtils.trim(e))
                        .state(SurveyRespondentState.INVITING)
                        .consent(false)
                        .archived(false)
                        .build()))
                .collect(toList());

        respondents.forEach(respondent -> {
            if (SurveyRespondentState.ERROR.equals(respondent.getState())) {
                respondent.setState(SurveyRespondentState.INVITING);
            }
            respondent.setInvitationLanguageTag(languageTag);
            respondent.setInvitationError(null);
            //TODO: change hashing to nullable
            respondent.setInvitationHash(
                    Hashing.sha512().hashString(String.format("%s-%s", survey, UUID.randomUUID()), StandardCharsets.UTF_8).toString());
        });

        applicationEventPublisher.publishEvent(new InviteToQuestionnaireEvent(
                surveyId,
                invitation.getSubject(),
                invitation.getBody(),
                surveyRespondentRepository.saveAll(respondents).stream()
                        .map(e -> new RespondentId(e.getId()))
                        .collect(collectingAndThen(toSet(), Collections::unmodifiableSet)),
                replyTo));
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<SurveyId, RespondentCounters>> countRespondentsAsync(@NonNull final OrganizationId organizationId) {
        return AsyncResult.forValue(surveyDao.countRespondents(organizationId));
    }

    //TODO: move to dao
    @NonNull
    @Transactional(readOnly = true)
    public Collection<Respondent> getAllRespondents(@NonNull final SurveyId surveyId) {
        return respondentAssembler.from(surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()));
    }

    //TODO: move to dao
    @NonNull
    @Transactional(readOnly = true)
    public Respondent getRespondent(@NonNull final RespondentId respondentId) {
        return respondentAssembler.from(surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found")));
    }

    //TODO: move to dao
    @NonNull
    @Transactional
    public OrganizationSurvey softDeleteSurvey(@NonNull final UUID surveyId) {
        final Survey survey = getSurveyOrThrow(surveyId);

        surveyRepository.delete(survey);

        final OrganizationSurvey organizationSurvey = organizationSurveyAssembler.from(survey);
        organizationSurvey.setVersion(survey.getVersion() + 1); // This only done so incremented version is returned to frontend
        organizationSurvey.setDeleted(true);

        return organizationSurvey;
    }

    @NonNull
    private Survey createOrCopySurvey(@Nullable final SurveyId sourceId,
                                      @NonNull final MultilingualText titles,
                                      @NonNull final MultilingualText descriptions,
                                      final List<String> languages) {
        final Survey survey;
        if (sourceId == null) {
            if (titles.isEmpty()) {
                throw new SurveyException("Title must not be empty");
            }

            final SurveyMetadata.SurveyMetadataBuilder metadata = copyMetadata(null)
                    .titles(titles.getPhrases())
                    .descriptions(descriptions.getPhrases())
                    .catalysts(surveyTemplateService.getDefaultCatalysts());



            survey = Survey.builder()
                    .version(1L)
                    .isDefault(false)
                    .state(SurveyState.OPEN)
                    .metadata(metadata.build())
                    .languages(languages)
                    .build();
        } else {
            survey = createCopy(sourceId, titles, descriptions, languages);
        }

        return surveyRepository.save(survey);
    }

    @NonNull
    private Survey getSurveyOrThrow(@NonNull final SurveyId id) {
        return getSurveyOrThrow(id.getValue());
    }

    @NonNull
    private Survey getSurveyOrThrow(@NonNull final UUID id) {
        return surveyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));
    }

    @EventListener
    @Async(ASYNC_EXECUTOR_NAME)
    public void handleOpenedEvent(@NonNull final QuestionnaireOpenedEvent event) {
        final IdValueObject<UUID> id = event.getRespondentId();

        if (id instanceof RespondentId) {
            final RespondentId respondentId = (RespondentId) id;
            if (!respondentDao.isInvited(respondentId)) {
                respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.OPENED);
            }
        } else if (id instanceof GuestId) {
            final GuestId guestId = (GuestId) id;
            //TODO: update status
        } else {
            throw new SurveyException("Invalid id");
        }
    }

    @NonNull
    private SurveyMetadata.SurveyMetadataBuilder copyMetadata(@Nullable final SurveyMetadata metadata) {
        if (metadata == null) {
            return SurveyMetadata.builder()
                    .hideCatalystThemePages(Boolean.FALSE)
                    .localisation(LocalisationMetadata.builder().build())
                    .translations(ImmutableMap.of());
        } else {
            return SurveyMetadata.builder()
                    .titles(metadata.getTitles())
                    .descriptions(metadata.getDescriptions())
                    .catalysts(metadata.getCatalysts())
                    .hideCatalystThemePages(Boolean.TRUE.equals(metadata.getHideCatalystThemePages()))
                    .parameters(metadata.getParameters())
                    .localisation(metadata.getLocalisation())
                    .translations(metadata.getTranslations());
        }
    }

}
