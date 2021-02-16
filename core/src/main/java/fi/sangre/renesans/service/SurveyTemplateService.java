package fi.sangre.renesans.service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import fi.sangre.renesans.application.assemble.SurveyTemplateAssembler;
import fi.sangre.renesans.application.model.OrganizationId;
import fi.sangre.renesans.application.model.SurveyState;
import fi.sangre.renesans.application.model.SurveyTemplate;
import fi.sangre.renesans.dto.CatalystDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static fi.sangre.renesans.application.utils.MultilingualUtils.compare;
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
    public List<SurveyTemplate> getTemplates(@NonNull final String languageTag) {
        return segmentService.getAllSegments()
                .stream()
                .map(e -> surveyTemplateAssembler.fromSegment(e, languageTag))
                .sorted((e1, e2) -> compare(e1.getTitles().getPhrases(), e2.getTitles().getPhrases(), languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @EventListener
    @Transactional
    public void importTemplates(@NonNull final ContextRefreshedEvent event) {
        final User admin = userRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        final Customer customer = getOrCreateCustomer(admin);

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

    @NonNull
    private Customer getOrCreateCustomer(@NonNull final User admin) {
        final OrganizationId id = new OrganizationId(UUID.fromString("b5d258fc-318c-4238-93da-22b1265b63dc"));
        final Customer customer = customerRepository.findById(id.getValue())
                .orElse(Customer.builder()
                        .id(id.getValue())
                        .name("Templates")
                        .description("Old segments from previous version of the app")
                        .owner(admin)
                        .createdBy(admin.getId())
                        .surveys(Sets.newHashSet())
                        .build());

        return customerRepository.saveAndFlush(customer);
    }

    private Survey importTemplate(@NonNull final SurveyTemplate template, @NonNull final Customer customer, @NonNull final User user) {
        final SurveyMetadata.SurveyMetadataBuilder metadata = SurveyMetadata
                .builder()
                .titles(template.getTitles().getPhrases())
                .descriptions(template.getDescriptions().getPhrases())
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
                    .questions(copyGenericAndSegmentQuestions(catalyst, template.getId().getValue()))
                    .build());
        }

        //TODO: throw on required
        metadata.catalysts(catalysts.build());

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
