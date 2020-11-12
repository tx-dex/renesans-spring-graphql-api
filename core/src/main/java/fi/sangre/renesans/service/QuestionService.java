package fi.sangre.renesans.service;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.aaa.UserPrincipalService;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.dto.DriverDto;
import fi.sangre.renesans.exception.CustomerNotFoundException;
import fi.sangre.renesans.exception.QuestionNotFoundException;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.graphql.input.QuestionInput;
import fi.sangre.renesans.graphql.input.WeightInput;
import fi.sangre.renesans.model.*;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor

@Service
public class QuestionService {
    private final QuestionRepository questionRepository;
    private final QuestionGroupRepository questionGroupRepository;
    private final MultilingualService multilingualService;
    private final WeightRepository weightRepository;
    private final SegmentRepository segmentRepository;
    private final CustomerRepository customerRepository;
    private final AnswerRepository answerRepository;
    private final SegmentQuestionGroupPhraseRepository segmentQuestionGroupPhraseRepo;
    private final CustomerDriverWeightsRepository customerDriverWeightsRepository;
    private final UserPrincipalService userPrincipalService;

    public QuestionGroup getCatalyst(final Long catalystId) {
        checkArgument(catalystId != null, "Catalyst id is required");

        return questionGroupRepository.findByIdAndParentIsNull(catalystId)
                .orElseThrow(() -> new ResourceNotFoundException("InvalidCatalystId", catalystId));
    }

    private CatalystDto getCatalyst(final QuestionGroup catalyst) {
        return CatalystDto.builder()
                .id(catalyst.getId())
                .titleId(catalyst.getTitleId())
                .pdfName(catalyst.getPdfName())
                .weight(catalyst.getWeight())
                .build();
    }

    private List<CatalystDto> getCatalysts(final List<QuestionGroup> catalysts, final Customer customer, final Segment segment) {
        checkArgument(catalysts != null, "Catalysts are required");

        final ImmutableList.Builder<CatalystDto> builder = ImmutableList.builder();

        final Map<Long, Long> catalystIdToTitleIdMap = getSegmentTitlesMap(segment);

        catalysts.stream()
                .sorted(Comparator.comparingLong(QuestionGroup::getSeq))
                .forEach(e -> {
                    final CatalystDto catalyst = getCatalyst(e);
                    catalyst.setCustomer(customer);
                    catalyst.setSegment(segment);
                    catalyst.setTitleId(catalystIdToTitleIdMap.getOrDefault(e.getId(), catalyst.getTitleId()));
                    builder.add(catalyst);
                });

        return builder.build();
    }

    public List<CatalystDto> getAllCatalysts() {
        return getCatalysts(questionGroupRepository.findAllByParentIsNull(), null, null);
    }

    @Transactional
    public List<CatalystDto> getCatalysts(final Survey survey) {
        return getCatalysts(this.questionGroupRepository.findAllByParentIsNull(),
                null,
                survey.getSegment());
    }

    @Transactional(readOnly = true)
    public List<CatalystDto> getCatalysts(final Customer customer) {
        return getCatalysts(this.questionGroupRepository.findAllByParentIsNull(),
                customer,
                customer != null ? customer.getSegment() : null);
    }

    @Transactional
    public List<CatalystDto> getCatalysts(final Segment segment) {
        return getCatalysts(this.questionGroupRepository.findAllByParentIsNull(),
                null,
                segment);
    }

    @Transactional
    public List<CatalystDto> getCatalysts(final RespondentGroup respondentGroup) {
        return getCatalysts(this.questionGroupRepository.findByRespondentGroupsContaining(respondentGroup),
                respondentGroup.getCustomer(),
                respondentGroup.getCustomer().getSegment());
    }

    @Transactional
    public List<DriverDto> getAllCatalystDrivers(final CatalystDto catalyst) {
        return getAllCatalystDrivers(catalyst.getId(), catalyst.getSegment(), catalyst.getCustomer());
    }

    @Transactional(readOnly = true)
    public List<DriverDto> getAllDrivers(final Customer customer) {
        final ImmutableList.Builder<DriverDto> builder = ImmutableList.builder();

        getCatalysts(customer).forEach(catalyst -> {
            builder.addAll(getAllCatalystDrivers(catalyst.getId(), segmentRepository.findByCustomers(customer).orElse(null), customer));
        });

        return builder.build();
    }

    @Transactional(readOnly = true)
    public List<DriverDto> getAllDrivers() {
        return questionGroupRepository.findAllByParentIsNotNull().stream()
                .sorted(Comparator.comparingLong(QuestionGroup::getSeq))
                .map(this::getDriver)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @Transactional(readOnly = true)
    public List<DriverDto> getAllCatalystDrivers(final Long catalystId, final Customer customer) {
        return getAllCatalystDrivers(catalystId, customer.getSegment(), customer);
    }

    private List<DriverDto> getAllCatalystDrivers(final Long catalystId, final Segment segment, final Customer customer) {
        checkArgument(catalystId != null, "Catalyst id is required");

        final ImmutableList.Builder<DriverDto> builder = ImmutableList.builder();

        final Map<Long, Long> driverIdToTitleIdMap = getSegmentTitlesMap(segment);
        final Map<Long, Double> driverIdToWeightMap = getCustomerDriverWeightMap(customer);

        this.questionGroupRepository.findByParentId(catalystId)
                .stream()
                .sorted(Comparator.comparingLong(QuestionGroup::getSeq))
                .forEach(e -> {
                    final DriverDto driver = getDriver(e);
                    driver.setTitleId(driverIdToTitleIdMap.getOrDefault(e.getId(), driver.getTitleId()));
                    driver.setWeight(driverIdToWeightMap.getOrDefault(e.getId(), driver.getWeight()));

                    builder.add(driver);
                });

        return builder.build();
    }

    private DriverDto getDriver(final QuestionGroup driver) {
        return DriverDto.builder()
                .id(driver.getId())
                .catalystId(driver.getParentId())
                .pdfName(driver.getPdfName())
                .titleId(driver.getTitleId())
                .descriptionId(driver.getDescriptionId())
                .prescriptionId(driver.getPrescriptionId())
                .weight(driver.getWeight())
                .build();
    }

    @NonNull
    @Transactional
    public Question getQuestion(final Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new QuestionNotFoundException(questionId));
    }

    @Transactional
    public Long getAnswerCount(final Question question) {
        checkArgument(question != null, "Question is required");

        return Optional.ofNullable(answerRepository.countByQuestionId(question.getId())).orElse(0L);
    }

    //TODO: add question source type
    public List<Question> getAllCatalystQuestions(final Long catalystId, final Customer customer, final Segment segment) {
        checkArgument(catalystId != null, "Catalyst id is required");
        final ImmutableList.Builder<Question> builder = ImmutableList.builder();

        builder.addAll(getCatalystGenericQuestions(catalystId));
        if (segment != null) {
            builder.addAll(getCatalystSegmentQuestions(catalystId, segment));
        }
        if (customer != null) {
            builder.addAll(getCatalystCustomerQuestions(catalystId, customer));
        }
        return builder.build();
    }

    public List<Question> getAllGenericQuestions() {
        return questionRepository.findAllBySourceType(Question.SourceType.GENERIC);
    }

    public List<Question> getCatalystGenericQuestions(final Long catalystId) {
        checkArgument(catalystId != null, "CatalystId is required");

        return questionRepository.findAllBySourceTypeAndQuestionGroupId(Question.SourceType.GENERIC, catalystId);
    }

    private List<Question> getCatalystSegmentQuestions(final Long catalystId, final Segment segment) {
        return questionRepository.findAllByQuestionGroupIdAndSegment(catalystId, segment);
    }

    public List<Question> getOnlySegmentQuestions(final Segment segment) {
        checkArgument(segment != null, "Segment is required");

        return ImmutableList.copyOf(questionRepository.findAllBySegment(segment));
    }

    public List<Question> getOnlySegmentQuestions(final Long segmentId) {
        checkArgument(segmentId != null, "Segment id is required");

        final Optional<Segment> segment = segmentRepository.findById(segmentId);

        if (segment.isPresent()) {
            return getOnlySegmentQuestions(segment.get());
        } else {
            return ImmutableList.of();
        }
    }

    public List<Question> getOnlyCustomerQuestions(final Customer customer) {
        checkArgument(customer != null, "Customer is required");

        return ImmutableList.copyOf(questionRepository.findAllByCustomer(customer));
    }

    public List<Question> getAllCustomerQuestions(final Customer customer) {
        checkArgument(customer != null, "Customer is required");

        final Set<Question> questions = Sets.newHashSet();

        questions.addAll(getAllGenericQuestions());

        if (customer.getSegment() != null) {
            questions.addAll(getOnlySegmentQuestions(customer.getSegment()));
        }

        questions.addAll(getOnlyCustomerQuestions(customer));

        return ImmutableList.copyOf(questions);
    }

    public List<Question> getAllCustomerQuestions(final Long customerId) {
        checkArgument(customerId != null, "Customer id is required");

        final Optional<Customer> customer = customerRepository.findById(customerId);
        if (customer.isPresent()) {
            return getAllCustomerQuestions(customer.get());
        } else {
            return ImmutableList.of();
        }
    }

    private List<Question> getCatalystCustomerQuestions(final Long catalystId, final Customer customer) {
        return questionRepository.findAllByQuestionGroupIdAndCustomer(catalystId, customer);
    }

    @Transactional
    public Question removeQuestion(final Long questionId) {
        final Question question = getQuestion(questionId);

        final UserPrincipal user = userPrincipalService.getLoggedInPrincipal();
        if (!userPrincipalService.isSuperUser(user) && question.getSourceType() != Question.SourceType.ORGANISATION) {
            throw new RuntimeException("User not allowed to remove this type of question");
        }

        question.setArchived(true);

        return questionRepository.save(question);
    }

    @Transactional
    public Question storeQuestion(final String languageCode, final QuestionInput questionInput) {
        return questionRepository.save(createOrUpdateQuestion(languageCode, questionInput, Question.SourceType.GENERIC));
    }

    @Transactional
    public Question storeCustomerQuestion(final String languageCode, final Long customerId, final QuestionInput questionInput) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));

        final Question question = createOrUpdateQuestion(languageCode, questionInput, Question.SourceType.ORGANISATION);
        question.setCustomer(customer);
        question.setCustomerId(customerId);

        return questionRepository.save(question);
    }

    @Transactional
    public Question storeSegmentQuestion(final String languageCode, final Long segmentId, final QuestionInput questionInput) {
        return storeSegmentQuestion(languageCode, segmentRepository.findById(segmentId)
                .orElseThrow(() -> new ResourceNotFoundException("InvalidSegmentId", segmentId)), questionInput);
    }

    Question storeSegmentQuestion(final String languageCode, final Segment segment, final QuestionInput questionInput) {
        checkArgument(segment != null, "Segment is required");

        final Question question = createOrUpdateQuestion(languageCode, questionInput, Question.SourceType.SEGMENT);
        question.setSegment(segment);

        return questionRepository.save(question);
    }

    private Question createOrUpdateQuestion(final String languageCode, final QuestionInput input, final Question.SourceType sourceType) {
        final Question question;
        if (input.getId() != null) { // update existing one
            question = questionRepository.findById(input.getId())
                    .orElseThrow(() -> new QuestionNotFoundException(input.getId()));
            if (input.getCatalystId() != null) {
                question.setQuestionGroup(getCatalyst(input.getCatalystId()));
            }
            if (question.getSourceType() != sourceType) {
                throw new IllegalArgumentException("Cannot change question source type");
            }
        } else { // create new one
            question = Question.builder()
                    .questionGroup(getCatalyst(input.getCatalystId()))
                    .sourceType(sourceType)
                    .build();
        }

        if (input.getTitle() != null) {
            final MultilingualKey titleKey = Optional.ofNullable(question.getTitle())
                    .orElse(MultilingualKey.builder().build());

            final MultilingualPhrase phrase = multilingualService.savePhrase(titleKey, input.getTitle(), languageCode);
            question.setTitle(phrase.getKey());
            question.setTitleId(phrase.getKey().getId());
        }

        if (input.getWeights() != null) {
            storeWeights(question, input.getWeights());
        }
        return question;
    }

    private void storeWeights(final Question question, List<WeightInput> inputs) {
        // Driver Id as a key
        final Map<Long, Weight> weights = question.getWeights().stream().collect(toMap(Weight::getQuestionGroupId, e -> e));

        inputs.forEach(e -> {
            if (e.getWeight() >= 0) {
                final Weight weight = weights.get(e.getQuestionGroupId());
                if (weight != null) {
                    weight.setWeight(e.getWeight());
                } else {
                    weights.putIfAbsent(e.getQuestionGroupId(), Weight.builder()
                            .question(question)
                            .questionGroupId(e.getQuestionGroupId())
                            .weight(e.getWeight())
                            .build());
                }
            }
        });

        question.getWeights().clear();
        question.getWeights().addAll(weightRepository.saveAll(weights.values()));
    }

    private Map<Long, Long> getSegmentTitlesMap(final Segment segment) {
        if (segment != null) {
            return segmentQuestionGroupPhraseRepo.findBySegment(segment).stream()
                    .collect(collectingAndThen(toMap(
                            SegmentQuestionGroupPhrase::getQuestionGroupId,
                            SegmentQuestionGroupPhrase::getTitleId
                    ), Collections::unmodifiableMap));
        } else {
            return ImmutableMap.of();
        }
    }

    private Map<Long, Double> getCustomerDriverWeightMap(final Customer customer) {
        if (customer != null) {
            return customerDriverWeightsRepository.findAllByCustomerId(customer.getId()).stream()
                    .collect(collectingAndThen(toMap(
                            CustomerDriverWeights::getDriverId,
                            CustomerDriverWeights::getWeight
                    ), Collections::unmodifiableMap));

        } else {
            return ImmutableMap.of();
        }
    }
}

