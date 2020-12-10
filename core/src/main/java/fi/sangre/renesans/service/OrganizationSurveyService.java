package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.hash.Hashing;
import fi.sangre.renesans.application.assemble.OrganizationSurveyAssembler;
import fi.sangre.renesans.application.assemble.RespondentAssembler;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.event.InviteRespondentsEvent;
import fi.sangre.renesans.application.merge.OrganizationSurveyMerger;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.respondent.Invitation;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.SurveyInput;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Weight;
import fi.sangre.renesans.persistence.model.TemplateId;
import fi.sangre.renesans.persistence.model.*;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
import fi.sangre.renesans.persistence.model.metadata.LocalisationMetadata;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.LikertQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.references.TemplateReference;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.persistence.repository.SurveyRespondentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static java.util.stream.Collectors.*;


@RequiredArgsConstructor
@Slf4j

@Service
public class OrganizationSurveyService {
    private final SurveyRepository surveyRepository;
    private final OrganizationSurveyAssembler organizationSurveyAssembler;
    private final OrganizationSurveyMerger organizationSurveyMerger;
    private final SurveyDao surveyDao;
    private final CustomerRepository customerRepository;
    private final QuestionService questionService;
    private final MultilingualService multilingualService;
    private final SurveyRespondentRepository surveyRespondentRepository;
    private final RespondentAssembler respondentAssembler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final MultilingualUtils multilingualUtils;

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
    public List<OrganizationSurvey> getSurveys(@NonNull final Organization organization, @NonNull final String languageTag) {
        return customerRepository.findById(organization.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"))
                .getSurveys().stream()
                .map(organizationSurveyAssembler::from)
                .sorted((e1,e2) -> compare(e1.getTitles().getPhrases(), e2.getTitles().getPhrases(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    @Transactional
    public OrganizationSurvey storeSurvey(@NonNull final UUID organizationId, @NonNull final SurveyInput input, @NonNull final String languageTag) {
        final Customer customer = customerRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));

        final Survey survey;

        if (input.getId() == null) { // create
            survey = createSurvey(customer, input, surveyRepository.findByIsDefaultTrue().orElse(null), languageTag);

            customer.getSurveys().add(survey);
            customerRepository.save(customer);
        } else { // update
            checkArgument(input.getVersion() != null, "input.version cannot be null");
            survey = surveyRepository.findById(input.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Survey not found"));

            final SurveyMetadata metadata = copy(survey.getMetadata());
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
            survey.setMetadata(metadata);

            surveyRepository.saveAndFlush(survey);
        }

        return organizationSurveyAssembler.from(survey);
    }

    @NonNull
    public OrganizationSurvey updateMetadata(@NonNull final OrganizationSurvey input) {
        final OrganizationSurvey updated = organizationSurveyMerger.combine(input);

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
        final Survey survey = getSurveyOrThrow(surveyId);

        final Map<String, SurveyRespondent> existing = surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()).stream()
                .filter(e -> invitation.getEmails().contains(e.getEmail()))
                .collect(toMap(SurveyRespondent::getEmail, e -> e));

        final List<SurveyRespondent> respondents = invitation.getEmails().stream()
                .map(e -> existing.getOrDefault(e, SurveyRespondent.builder()
                        .surveyId(surveyId.getValue())
                        .email(StringUtils.trim(e))
                        .state(SurveyRespondentState.INVITING)
                        .consent(false)
                        .archived(false)
                        .build()))
                .collect(toList());

        respondents.forEach(respondent -> {
            respondent.setState(SurveyRespondentState.INVITING);
            respondent.setInvitationError(null);
            //TODO: change hashing to nullable
            respondent.setInvitationHash(
                    Hashing.sha512().hashString(String.format("%s-%s", survey, UUID.randomUUID()), StandardCharsets.UTF_8).toString());
        });

        applicationEventPublisher.publishEvent(new InviteRespondentsEvent(
                surveyId,
                invitation.getSubject(),
                invitation.getBody(),
                surveyRespondentRepository.saveAll(respondents).stream()
                        .map(e -> new RespondentId(e.getId()))
                        .collect(collectingAndThen(toSet(), Collections::unmodifiableSet)),
                replyTo));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Collection<Respondent> getAllRespondents(@NonNull final SurveyId surveyId) {
        return respondentAssembler.from(surveyRespondentRepository.findAllBySurveyId(surveyId.getValue()));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Respondent getRespondent(@NonNull final RespondentId respondentId, @NonNull final String invitationHash) {
        return respondentAssembler.from(surveyRespondentRepository.findByIdAndInvitationHash(respondentId.getValue(), invitationHash)
                .orElseThrow(() -> new SurveyException("Respondent not found")));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Respondent getRespondent(@NonNull final RespondentId respondentId) {
        return respondentAssembler.from(surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found")));
    }

    @NonNull
    @Transactional
    public Respondent softDeleteRespondent(@NonNull final RespondentId respondentId) {
        final SurveyRespondent respondent = surveyRespondentRepository.findById(respondentId.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        surveyRespondentRepository.delete(respondent);

        return respondentAssembler.from(respondent);
    }

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
    private Survey createSurvey(@NonNull final Customer customer, SurveyInput input, @Nullable final Survey defaultSurvey, @NonNull final String languageTag) {
        final MultilingualText titles = multilingualUtils.create(input.getTitle(), languageTag);
        final MultilingualText descriptions = multilingualUtils.create(input.getDescription(), languageTag);

        if (titles.isEmpty()) {
            throw new SurveyException("Title must not be empty");
        }

        final SurveyMetadata.SurveyMetadataBuilder metadata = SurveyMetadata
                .builder()
                .titles(titles.getPhrases())
                .descriptions(descriptions.getPhrases())
                .localisation(LocalisationMetadata.builder().build())
                .translations(ImmutableMap.of());

        final ImmutableList.Builder<CatalystMetadata> catalysts = ImmutableList.builder();

        for (final CatalystDto catalyst : questionService.getCatalysts(customer)) {
            final List<DriverMetadata> drivers = questionService.getAllCatalystDrivers(catalyst.getOldId(), customer)
                    .stream()
                    .map(driver -> DriverMetadata.builder()
                            .id(driver.getId())
                            .pdfName(driver.getPdfName())
                            .titles(multilingualService.getPhrases(driver.getTitleId()))
                            .descriptions(multilingualService.getPhrases(driver.getDescriptionId()))
                            .prescriptions(multilingualService.getPhrases(driver.getPrescriptionId()))
                            .weight(driver.getWeight())
                            .build())

                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            catalysts.add(CatalystMetadata.builder()
                    .id(catalyst.getId().getValue())
                    .pdfName(catalyst.getPdfName())
                    .titles(multilingualService.getPhrases(catalyst.getTitleId()))
                    .weight(catalyst.getWeight())
                    .drivers(drivers)
                    .questions(copyGenericAndSegmentQuestions(catalyst, input.getTemplateId()))
                    .build());
        }

        //TODO: throw on required
        metadata.catalysts(catalysts.build());

        return surveyRepository.save(Survey.builder()
                .version(1L)
                .isDefault(false)
                .metadata(metadata.build())
                .build());
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

    @NonNull
    private SurveyMetadata copy(@Nullable final SurveyMetadata metadata) {
        if (metadata == null) {
            return SurveyMetadata.builder().build();
        } else {
            return SurveyMetadata.builder()
                    .titles(metadata.getTitles())
                    .descriptions(metadata.getDescriptions())
                    .catalysts(metadata.getCatalysts())
                    .parameters(metadata.getParameters())
                    .localisation(metadata.getLocalisation())
                    .translations(metadata.getTranslations())
                    .build();
        }
    }

    @NonNull
    private List<QuestionMetadata> copyGenericAndSegmentQuestions(@NonNull final CatalystDto catalyst, @Nullable final Long segmentId) {
        final ImmutableList.Builder<QuestionMetadata> questions = ImmutableList.<QuestionMetadata>builder()
                .addAll(questionService.getCatalystGenericQuestions(catalyst.getOldId())
                .stream()
                .map(this::fromGenericQuestion)
                .collect(toList()));

        if (segmentId != null) {
                questions.addAll(questionService.getCatalystSegmentQuestions(catalyst.getOldId(), segmentId)
                        .stream()
                        .map(e -> fromSegmentQuestion(e, segmentId))
                        .collect(toList()));
        }

        return questions.build();
    }

    private QuestionMetadata fromGenericQuestion(@NonNull final Question question) {
        return LikertQuestionMetadata.builder()
                .id(UUID.randomUUID())
                .titles(multilingualService.getPhrases(question.getTitleId()))
                .driverWeights(getQuestionDriverWeights(question))
                .build();
    }

    private QuestionMetadata fromSegmentQuestion(@NonNull final Question question, @NonNull final Long segmentId) {
        return LikertQuestionMetadata.builder()
                .id(UUID.randomUUID())
                .titles(multilingualService.getPhrases(question.getTitleId()))
                .reference( new TemplateReference(new TemplateId(segmentId), 1L))
                .driverWeights(getQuestionDriverWeights(question))
                .build();
    }

    private Map<String, Double> getQuestionDriverWeights(@NonNull final Question question) {
        return question.getWeights().stream()
                .collect(collectingAndThen(toMap(
                        e -> e.getQuestionGroupId().toString(),
                        Weight::getWeight
                ), Collections::unmodifiableMap));
    }
}
