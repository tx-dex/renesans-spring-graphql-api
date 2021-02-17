package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.assemble.SurveyTemplateAssembler;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.SurveyState;
import fi.sangre.renesans.application.model.SurveyTemplate;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Weight;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.TemplateId;
import fi.sangre.renesans.persistence.model.User;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.persistence.model.metadata.DriverMetadata;
import fi.sangre.renesans.persistence.model.metadata.LocalisationMetadata;
import fi.sangre.renesans.persistence.model.metadata.SurveyMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.LikertQuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.questions.QuestionMetadata;
import fi.sangre.renesans.persistence.model.metadata.references.TemplateReference;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.persistence.repository.SurveyRepository;
import fi.sangre.renesans.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
import static fi.sangre.renesans.config.ApplicationConfig.ASYNC_EXECUTOR_NAME;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Service
public class SurveyTemplateService {
    private final SegmentService segmentService;
    private final SurveyTemplateAssembler surveyTemplateAssembler;
    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final SurveyRepository surveyRepository;
    private final QuestionService questionService;
    private final MultilingualService multilingualService;

    @NonNull
    private List<SurveyTemplate> getTemplates(@NonNull final String languageTag) {
        return segmentService.getAllSegments()
                .stream()
                .map(e -> surveyTemplateAssembler.fromSegment(e, languageTag))
                .sorted((e1, e2) -> compare(e1.getTitles().getPhrases(), e2.getTitles().getPhrases(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Async(ASYNC_EXECUTOR_NAME)
    @EventListener
    @Transactional
    public void importTemplates(@NonNull final ContextRefreshedEvent event) {
        final User admin = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        final OrganizationId id = new OrganizationId(UUID.fromString("b5d258fc-318c-4238-93da-22b1265b63dc"));
        final Customer customer = customerRepository.findById(id.getValue())
                .orElseThrow(() -> new SurveyException("Cannot find template organization"));

        if (customer.getSurveys().isEmpty()) {
            log.info("Importing templates");

            getTemplates("en").forEach(template -> {
                final Survey survey = importTemplate(template, customer, admin);

                customer.getSurveys().add(survey);

                customerRepository.save(customer);
            });
        } else {
            log.info("Templates already imported");
        }
    }

    private Survey importTemplate(@NonNull final SurveyTemplate template, @NonNull final Customer customer, @NonNull final User user) {
        final List<CatalystMetadata> catalysts = getSegmentCatalysts(customer, template.getId().getValue());

        final SurveyMetadata.SurveyMetadataBuilder metadata = SurveyMetadata
                .builder()
                .titles(template.getTitles().getPhrases())
                .descriptions(template.getDescriptions().getPhrases())
                .catalysts(catalysts)
                .localisation(LocalisationMetadata.builder().build())
                .translations(ImmutableMap.of());

        return surveyRepository.save(Survey.builder()
                .version(1L)
                .isDefault(false)
                .state(SurveyState.OPEN)
                .metadata(metadata.build())
                .craetedBy(user.getId())
                .modifiedBy(user.getId())
                .build());
    }

    @NonNull
    public List<CatalystMetadata> getDefaultCatalysts() {
        return getSegmentCatalysts(null, null);
    }

    @NonNull
    private List<CatalystMetadata> getSegmentCatalysts(@Nullable final Customer customer, @Nullable final Long templateId) {
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
                    .questions(copyGenericAndSegmentQuestions(catalyst, templateId))
                    .build());
        }

        return catalysts.build();
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

    @NonNull
    private QuestionMetadata fromGenericQuestion(@NonNull final Question question) {
        return LikertQuestionMetadata.builder()
                .id(UUID.randomUUID())
                .titles(multilingualService.getPhrases(question.getTitleId()))
                .driverWeights(getQuestionDriverWeights(question))
                .build();
    }

    @NonNull
    private QuestionMetadata fromSegmentQuestion(@NonNull final Question question, @NonNull final Long segmentId) {
        return LikertQuestionMetadata.builder()
                .id(UUID.randomUUID())
                .titles(multilingualService.getPhrases(question.getTitleId()))
                .reference(new TemplateReference(new TemplateId(segmentId), 1L))
                .driverWeights(getQuestionDriverWeights(question))
                .build();
    }

    @NonNull
    private Map<String, Double> getQuestionDriverWeights(@NonNull final Question question) {
        return question.getWeights().stream()
                .collect(collectingAndThen(toMap(
                        e -> e.getQuestionGroupId().toString(),
                        Weight::getWeight
                ), Collections::unmodifiableMap));
    }
}
