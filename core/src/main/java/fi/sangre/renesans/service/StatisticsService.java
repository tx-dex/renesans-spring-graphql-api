package fi.sangre.renesans.service;

import com.google.common.collect.*;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.CatalystStatistics;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.dto.DriverDto;
import fi.sangre.renesans.dto.FiltersDto;
import fi.sangre.renesans.exception.InputArgumentsValidationException;
import fi.sangre.renesans.exception.RespondentGroupNotFoundException;
import fi.sangre.renesans.exception.RespondentNotFoundException;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.model.Segment;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.model.Survey;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.repository.AnswerRepository;
import fi.sangre.renesans.repository.RespondentGroupRepository;
import fi.sangre.renesans.repository.RespondentRepository;
import fi.sangre.renesans.statistics.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static fi.sangre.renesans.application.utils.StatisticsUtils.MAX_ANSWER_VALUE;
import static java.util.stream.Collectors.*;
import static fi.sangre.renesans.application.utils.StatisticsUtils.indexToRate;

@Slf4j
@RequiredArgsConstructor

@Service
public class StatisticsService {
    public final static Comparator<StatisticsQuestion> QUESTION_COMPARATOR = Comparator.comparingDouble(e -> e.getAnswer().getAvg() != null ? e.getAnswer().getAvg() : 0);
    private final static Double MEDIAN_PERCENTILE_VALUE = 50d;
    private final static Double DEFAULT_WEIGHT_VALUE = 1d;
    private final static Double NORMALIZED_MIN_ANSWER_VALUE = 0.2;
    private final static Double NORMALIZED_RESULT_RANGE = 0.8;
    private final static String EMPTY_FIELD = "-";

    private final AnswerRepository answerRepository;
    private final RespondentRepository respondentRepository;
    private final RespondentService respondentService;
    private final CustomerRepository customerRepository;
    private final RespondentGroupRepository respondentGroupRepository;
    private final SurveyService surveyService;
    private final MultilingualService multilingualService;
    private final QuestionService questionService;
    private final CustomerService customerService;
    private final RespondentGroupService respondentGroupService;
    private final SurveyUtils surveyUtils;

    @Deprecated
    @Transactional(readOnly = true)
    public Statistics statistics(final FiltersDto filters, final String languageCode) {
        final Survey survey = surveyService.getDefaultSurvey();
        final FiltersDto newFilters = filters != null ? filters : new FiltersDto();
        final Set<Question> questions = getQuestionsBasedOnFilter(newFilters.getSegmentIds(), newFilters.getCustomerIds(), newFilters.getRespondentGroupIds(), newFilters.getRespondentIds());
        return calculateStatistics(survey, questions, newFilters, languageCode);
    }

    @Deprecated
    @Transactional(readOnly = true)
    public ComparativeStatistics comparativeStatistics(FiltersDto filters,
                                                       final List<UUID> customerIds,
                                                       final List<String> respondentGroupIds,
                                                       final List<String> respondentIds,
                                                       final Boolean edit,
                                                       final String languageCode) {

        if (!edit && !Range.closed(2, 10).contains(
                getUniqueCount(customerIds)
                        + getUniqueCount(respondentGroupIds)
                        + getUniqueCount(respondentIds)
        )) {
            log.warn("At least 2 or at most 10 ids must be selected for comparative report, customerIds={}, respondentGroupIds={}, respondentIds={}", customerIds, respondentGroupIds, respondentIds);
            throw new InputArgumentsValidationException("At least 2 or at most 10 ids must be selected for comparative report");
        }

        final Survey survey = surveyService.getDefaultSurvey();

        if (filters == null) {
            filters = new FiltersDto();
        }

        final Set<Question> questions = getQuestionsBasedOnFilter(null, customerIds, respondentGroupIds, respondentIds);

        final List<Statistics> customerStatistics = calculateComparativeStatisticForCustomers(survey, questions, customerIds, filters, languageCode);
        final List<Statistics> respondentGroupStatistics = calculateComparativeStatisticForRespondentGroups(survey, questions, respondentGroupIds, filters, languageCode);
        final List<Statistics> respondentStatistics = calculateComparativeStatisticForRespondents(survey, questions, respondentIds, languageCode);

        return new ComparativeStatistics(
                customerStatistics,
                respondentGroupStatistics,
                respondentStatistics);
    }

    @Transactional
    public Statistics calculateStatisticForRespondent(final Respondent respondent){
        final Survey survey = surveyService.getDefaultSurvey();
        FiltersDto newFilters = new FiltersDto();
        final List<String> respondentIds = ImmutableList.of(respondent.getId());
        newFilters.setRespondentIds(respondentIds);
        final Set<Question> questions = getQuestionsBasedOnFilter(null, null, ImmutableList.of(respondent.getRespondentGroupId()), null);
        return calculateStatistics(survey, questions, newFilters, null);
    }

    public List<LocalizedCatalyst> getCatalysts(final Customer customer, final String languageCode) {
        return questionService.getCatalysts(customer).stream().map(e -> LocalizedCatalyst.builder()
                    .id(e.getOldId())
                    .name(multilingualService.lookupPhrase(e.getTitleId(), languageCode))
                    .drivers(getDrivers(e, languageCode))
                .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public List<LocalizedCatalyst> getCatalysts(final Segment segment, final String languageCode) {
        return questionService.getCatalysts(segment).stream().map(e -> LocalizedCatalyst.builder()
                .id(e.getOldId())
                .name(multilingualService.lookupPhrase(e.getTitleId(), languageCode))
                .drivers(getDrivers(e, languageCode))
                .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private List<LocalizedDriver> getDrivers(final CatalystDto catalyst, final String languageCode) {
        return questionService.getAllCatalystDrivers(catalyst).stream().map(e -> LocalizedDriver.builder()
                .id(e.getId())
                .name(multilingualService.lookupPhrase(e.getTitleId(), languageCode))
                .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    //Finds only intersection of  the questions that apply to all
    private Set<Question> getQuestionsBasedOnFilter(final List<Long> segmentIds, final List<UUID> customerIds, final List<String> respondentGroupIds, final List<String> respondentIds) {
        Set<Question> questions = null;

        final Set<Customer> customers = Sets.newHashSet();

        if (respondentIds != null && respondentIds.size() > 0) {
            respondentGroupService.getRespondentGroupsForRespondentsInternally(respondentIds).forEach( e -> customers.add(e.getCustomer()));
        }

        if (respondentGroupIds != null) {
            customers.addAll(respondentGroupIds.stream().filter(Objects::nonNull).distinct()
                    .map(respondentGroupId -> respondentGroupService.getRespondentGroup(respondentGroupId)
                            .getCustomer())
                    .filter(Objects::nonNull)
                    .collect(toSet()));
        }

        if (customerIds != null) {
            customers.addAll(customerIds.stream().filter(Objects::nonNull).distinct()
                    .map(customerService::getCustomer)
                    .collect(toSet()));

        }

        for(final Customer customer : customers) {
            questions = getIntersectionOfQuestions(questions, questionService.getAllCustomerQuestions(customer));
        }

        //TODO: use segment in for filtering questions
//        if (segmentIds != null) {
//            segmentIds.stream().distinct().forEach(e -> getIntersectionOfQuestions(questions, questionService.getOnlySegmentQuestions(e)));
//        }

        if (questions != null) {
            return ImmutableSet.copyOf(questions);
        } else {
            return ImmutableSet.of();
        }
    }

    private Set<Question> getIntersectionOfQuestions(final Set<Question> questions, Collection<Question> newQuestions) {
        if (questions == null) {
            return Sets.newHashSet(newQuestions);
        } else {
            questions.retainAll(newQuestions); //removes the questions that were added before but are not in newQuestions collection
            return questions;
        }
    }

    private void applyDriverWeightModifier(@NonNull final Map<DriverId, DriverStatistics> driversStatistics, @NonNull final Map<DriverId, Double> driverWeights) {
        final double[] weights = driverWeights.values().stream().mapToDouble(e -> e != null ? e : DEFAULT_WEIGHT_VALUE).sorted().toArray();

        final DescriptiveStatistics statistics = new DescriptiveStatistics(weights);
        final Double driverWeightMedian = statistics.getPercentile(MEDIAN_PERCENTILE_VALUE);
        final Double driverWeightSum = statistics.getSum();
        final Double driverWeightMax = statistics.getMax();

        for(final Map.Entry<DriverId, DriverStatistics> entry : driversStatistics.entrySet()) {
            final Double driverWeight = driverWeights.getOrDefault(entry.getKey(), DEFAULT_WEIGHT_VALUE);

            entry.getValue().setWeight(driverWeight / driverWeightSum);
            entry.getValue().setWeightModifier(driverWeight / driverWeightMedian);
            entry.getValue().setImportance(driverWeight / driverWeightMax);
        }
    }

    private List<DriverStatistics> calculateDriversStatistics(@NonNull final OrganizationSurvey survey, @NonNull final Map<QuestionId, QuestionStatistics> questionStatistics) {
        final Map<DriverId, DriverStatistics> driversStatistics = Maps.newHashMap();
        final Map<DriverId, Double> driverWeights = Maps.newHashMap();
        final Map<DriverId, Double> sumOfQuestionsWeightsPerDriver = Maps.newHashMap();
        final Map<DriverId, Double> sumOfQuestionsResultsPerDriver = Maps.newHashMap();
        final Map<DriverId, Double> sumOfQuestionsWeighedResultsPerDriver = Maps.newHashMap();

        final Map<QuestionId, Map<DriverId, Double>> questionWeights = surveyUtils.getAllQuestions(survey).stream()
                .collect(collectingAndThen(toMap(
                        LikertQuestion::getId,
                        LikertQuestion::getWeights,
                        (v1, v2) -> v1
                ), Collections::unmodifiableMap));

        // Prepare temporary maps
        survey.getCatalysts().forEach(catalyst -> catalyst.getDrivers()
            .forEach(driver -> {
                final DriverId driverId = new DriverId(driver.getId());
                driversStatistics.put(driverId, DriverStatistics.builder()
                        .id(driverId)
                        .titles(driver.getTitles().getPhrases())
                        .catalystId(catalyst.getId())
                        .build());
                driverWeights.put(driverId, driver.getWeight());

                sumOfQuestionsWeightsPerDriver.put(driverId, 0d);
                sumOfQuestionsResultsPerDriver.put(driverId, 0d);
                sumOfQuestionsWeighedResultsPerDriver.put(driverId, 0d);
            }));

        applyDriverWeightModifier(driversStatistics, driverWeights);

        // Sums all question results and weights per driver to calculate weighted average after all
        for (final Map.Entry<QuestionId, QuestionStatistics> entry : questionStatistics.entrySet()) {
            final Double questionAverage = entry.getValue().getAvg();

            // do not count skipped question, null is set for questions that does not concern respondents.
            // This is only case when it can be null, as we always calculate stats for respondents that finished questionnaire
            if (questionAverage != null) {
                final Map<DriverId, Double> weights = questionWeights.get(entry.getKey());

                weights.forEach((driverId, questionWeight) -> {
                    if (questionWeight > 0) { //  weight = 0 can be skipped as it does not change results
                        final Double driverWeightModifier = driversStatistics.get(driverId).getWeightModifier();
                        final Double questionRate = questionAverage / MAX_ANSWER_VALUE;

                        sumOfQuestionsWeightsPerDriver.computeIfPresent(driverId, (k, v) -> v + questionWeight);
                        sumOfQuestionsResultsPerDriver.computeIfPresent(driverId, (k, v) -> v + (questionWeight * questionRate));
                        sumOfQuestionsWeighedResultsPerDriver.computeIfPresent(driverId, (k, v) -> v + (questionWeight * calculateWeighedQuestionResult(questionRate, driverWeightModifier)));
                    }
                });
            }
        }

        // Just calculates weighted average for each driver
        for (final Map.Entry<DriverId, DriverStatistics> entry : driversStatistics.entrySet()) {
            final Double driverQuestionWeightsSum = sumOfQuestionsWeightsPerDriver.get(entry.getKey());
            final Double driverQuestionResultsSum = sumOfQuestionsResultsPerDriver.get(entry.getKey());
            final Double driverQuestionWeighedResultSum = sumOfQuestionsWeighedResultsPerDriver.get(entry.getKey());

            if (driverQuestionWeightsSum > 0d) {
                entry.getValue().setResult(driverQuestionResultsSum / driverQuestionWeightsSum);
                entry.getValue().setWeighedResult(driverQuestionWeighedResultSum / driverQuestionWeightsSum);
            }
        }

        return ImmutableList.copyOf(driversStatistics.values());
    }

    private List<CatalystStatistics> calculateCatalystsStatistics(@NonNull final OrganizationSurvey survey,
                                                                  @NonNull final List<DriverStatistics> driverStatistics,
                                                                  @NonNull final Map<QuestionId, QuestionStatistics> questionStatistics) {
        final double allDriverWeightSum = driverStatistics.stream().mapToDouble(DriverStatistics::getWeight).sum();

        final ImmutableList.Builder<CatalystStatistics> builder = ImmutableList.builder();

        survey.getCatalysts().forEach(catalyst -> {
            final Set<QuestionId> questionsIds = catalyst.getQuestions().stream()
                    .map(LikertQuestion::getId)
                    .collect(toSet());

            final Map<QuestionId, QuestionStatistics> catalystQuestions = questionStatistics.entrySet()
                    .stream()
                    .filter(e -> questionsIds.contains(e.getKey()))
                    .collect(collectingAndThen(toMap(
                            Map.Entry::getKey,
                            e -> QuestionStatistics.builder()
                                    .questionId(e.getValue().getQuestionId())
                                    .rate(e.getValue().getRate())
                                    .avg(indexToRate(e.getValue().getAvg()))
                                    .max(indexToRate(e.getValue().getMax()))
                                    .min(indexToRate(e.getValue().getMin()))
                                    .count(e.getValue().getCount())
                            .build(),
                            (e1, e2) -> e1
                    ), Collections::unmodifiableMap));

            final Set<DriverId> driverIds = catalyst.getDrivers().stream()
                    .map(Driver::getId)
                    .map(DriverId::new)
                    .collect(toSet());
            final List<DriverStatistics> catalystDriversStatistics = driverStatistics.stream()
                    .filter(e -> driverIds.contains(e.getId()))
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            final Double catalystWeight = catalystDriversStatistics.stream().mapToDouble(DriverStatistics::getWeight).sum() / allDriverWeightSum;
            final Double catalystResult = catalystDriversStatistics.stream().mapToDouble(e -> e.getResult() != null ? e.getResult() : 0d).sum() / catalystDriversStatistics.size();
            final Double catalystWeighedResult = catalystDriversStatistics.stream().mapToDouble(e -> e.getWeighedResult() != null ? e.getWeighedResult() : 0d).sum() / catalystDriversStatistics.size();

            builder.add(CatalystStatistics.builder()
                    .id(catalyst.getId())
                    .titles(catalyst.getTitles().getPhrases())
                    .drivers(catalystDriversStatistics.stream().collect(collectingAndThen(toMap(
                            DriverStatistics::getId,
                            v -> v,
                            (v1, v2) -> v1
                    ), Collections::unmodifiableMap)))
                    .questions(catalystQuestions)
                    .result(catalystResult)
                    .weighedResult(catalystWeighedResult)
                    .weight(catalystWeight)
                    .build());
        });

        final List<CatalystStatistics> catalysts = builder.build();

        final DescriptiveStatistics statistics = new DescriptiveStatistics(catalysts.stream().mapToDouble(CatalystStatistics::getWeight).toArray());
        final Double catalystWeightMax = statistics.getMax();

        catalysts.forEach(e -> {
            e.setImportance(e.getWeight() / catalystWeightMax);
        });

        return catalysts;
    }

    private Double calculateTotalResult(final List<CatalystStatistics> catalysts) {
        final List<CatalystStatistics> enabledCatalysts = catalysts.stream()
                .filter(catalyst -> catalyst.getResult() > 0)
                .collect(toList());

        final double ratio = enabledCatalysts.size() > 0 ? enabledCatalysts.size() : 1d;

        return catalysts.stream().mapToDouble(CatalystStatistics::getWeighedResult).sum() / ratio;
    }

    private Map<Question, StatisticsAnswer> getAnswerStatistics(final Set<Question> questions, final List<Respondent> respondents) {
        if (respondents.size() > 0) {
            return questions.stream().collect(collectingAndThen(toMap(
                    e -> e,
                    e -> Optional.ofNullable(answerRepository.findAnswerStatisticsByQuestionAndRespondentsIn(e.getId(), respondents))
                            .orElse(StatisticsAnswer.builder().questionId(e.getId()).count(0L).build())
            ), Collections::unmodifiableMap));
        } else { // return just empty stats
            return questions.stream().collect(collectingAndThen(toMap(
                    e -> e,
                    e -> StatisticsAnswer.builder().questionId(e.getId()).count(0L).build()
            ), Collections::unmodifiableMap));
        }
    }

    private List<Segment> getSegmentsForRespondents(final List<Respondent> respondents) {
        final Set<String> respondentGroupIds = respondents.stream()
                .map(Respondent::getRespondentGroupId)
                .collect(toSet());

        return customerRepository.findAllByGroupsIdIn(respondentGroupIds).stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(Customer::getSegment)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    private Segment getSegmentFromFilter(FiltersDto filter) {
        if (filter != null) {
            //TODO: use services directly
            if (filter.getCustomerIds() != null && filter.getCustomerIds().size() == 1) {
                return customerService.getCustomer(filter.getCustomerIds().get(0)).getSegment();
            } else if (filter.getRespondentGroupIds() != null && filter.getRespondentGroupIds().size() == 1) {
                return respondentGroupRepository.findById(filter.getRespondentGroupIds().get(0))
                        .orElseThrow(() -> new RespondentGroupNotFoundException(filter.getRespondentGroupIds().get(0)))
                        .getCustomer().getSegment();
            } else if (filter.getRespondentIds() != null && filter.getRespondentIds().size() == 1) {
                return respondentRepository.findById(filter.getRespondentIds().get(0))
                        .orElseThrow(() -> new RespondentNotFoundException(filter.getRespondentIds().get(0)))
                        .getRespondentGroup().getCustomer().getSegment();
            }
        }
        return null;
    }

    private Customer getCustomerForRespondents(final List<Respondent> respondents) {
        final List<Customer> customers = respondents.stream()
                .map(Respondent::getRespondentGroup).distinct()
                .map(RespondentGroup::getCustomer).distinct()
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));

        if (customers.size() == 1) {
            return customers.get(0);
        } else {
            return null;
        }
    }

    /**
     * Returns generic names if there are not segments found or there are more then 1 segments,
     * or returns names for the segment if only one is found
     *
     * @param segmentFromFilter segment taken from filter
     * @param segmentsFromRespondents segments found based on the answers and respondents
     * @param languageCode language code
     * @return map between catalyst/driver id and names taken for the report
     */
    private Map<Long, String> getCatalystAndDriversNames(final Segment segmentFromFilter,
                                                         final List<Segment> segmentsFromRespondents,
                                                         final String languageCode) {

        // Get original names for catalyst and drivers
        final HashMap<Long, String> originalCatalystAndDriversNames = new HashMap<>();
        getCatalysts((Segment) null, languageCode).forEach(catalyst -> {
            originalCatalystAndDriversNames.computeIfAbsent(catalyst.getId(), k -> catalyst.getName());
            catalyst.getDrivers().forEach(driver -> {
                originalCatalystAndDriversNames.computeIfAbsent(driver.getId(), k -> driver.getName());
            });
        });

        final Set<Segment> foundSegments = segmentsFromRespondents.stream()
                .filter(Objects::nonNull).collect(toSet());
        if (segmentFromFilter != null) {
            foundSegments.add(segmentFromFilter);
        }

        // Get segments names for catalysts and drivers
        final Map<Long, String> segmentCatalystsAndDriverNames = new HashMap<>();
        if (foundSegments.size() == 1) {
            {
                foundSegments.forEach(segment -> {
                    getCatalysts(segment, languageCode).forEach(catalyst -> {
                        segmentCatalystsAndDriverNames.put(catalyst.getId(), catalyst.getName());

                        catalyst.getDrivers().forEach(
                                driver -> segmentCatalystsAndDriverNames.put(driver.getId(), driver.getName())
                        );
                    });
                });
            }
        }

        if (segmentCatalystsAndDriverNames.isEmpty()) { // use original names, not segments found
            return ImmutableMap.copyOf(originalCatalystAndDriversNames);
        } else if (segmentsFromRespondents.contains(null)) { // there was customer with no segment return only original names
            return ImmutableMap.copyOf(originalCatalystAndDriversNames);
        } else { // use only segment names
            return ImmutableMap.copyOf(segmentCatalystsAndDriverNames);
        }
    }

    // TODO refactor
    private String getCustomerNames(FiltersDto filters) {
        // make name string from customer ids...
        if (filters.getSurveyId() != null && filters.getCustomerIds() != null && filters.getCustomerIds().size() > 0) {
            List<Customer> customers = customerRepository.findAllById(filters.getCustomerIds());
            if (customers.size() > 0) {
                return String.join(", ", customers.stream().map(Customer::getName).distinct().collect(toList()));
            }
        // .. or from respondent ids ..
        } else if (filters.getRespondentGroupIds() != null && filters.getRespondentGroupIds().size() > 0) {
            List<RespondentGroup> respondentGroups = respondentGroupRepository.findAllById(filters.getRespondentGroupIds());
            if (respondentGroups.size() > 0) {
                return String.join(", ", respondentGroups.stream().map(respondentGroup -> respondentGroup.getCustomer().getName()).distinct().collect(toList()));
            }
        // .. or from selection of respondents
        } else if (filters.getRespondentIds() != null && filters.getRespondentIds().size() > 0) {
            List<Respondent> respondents = respondentRepository.findAllById(filters.getRespondentIds());
            if (respondents.size() > 0) {
                return String.join(", ", respondents.stream().map(respondent -> respondent.getRespondentGroup().getCustomer().getName()).distinct().collect(toList()));
            }
        }

        return EMPTY_FIELD;
    }

    // TODO refactor
    private String getRespondentGroupNames(FiltersDto filters) {
        // make name string from respondent group ids...
        if (filters.getRespondentGroupIds() != null && filters.getRespondentGroupIds().size() > 0) {
            List<RespondentGroup> respondentGroups = respondentGroupRepository.findAllById(filters.getRespondentGroupIds());
            if (respondentGroups.size() > 0) {
                return String.join(", ", respondentGroups.stream().map(RespondentGroup::getTitle).distinct().collect(toList()));
            }
        // .. or from selection of respondents
        } else if (filters.getRespondentIds() != null && filters.getRespondentIds().size() > 0) {
            List<Respondent> respondents = respondentRepository.findAllById(filters.getRespondentIds());
            if (respondents.size() > 0) {
                return String.join(", ", respondents.stream().map(respondent -> respondent.getRespondentGroup().getTitle()).distinct().collect(toList()));
            }
        }

        return EMPTY_FIELD;
    }

    private String getName(final List<Respondent> respondents, final FiltersDto filters, final String languageCode) {

        String name;
        // generate name from respondent ids...
        if (filters.getRespondentIds() != null && filters.getRespondentIds().size() > 0) {
            name = String.join(", ", respondents.stream().map(Respondent::getName).collect(toList()));
        // ... or check if the report is filtered or not and print out static text
        } else if (filters.hasActiveProfileFilters()) {
            name = multilingualService.lookupPhrase("statistics_result_filtered", languageCode);
        } else {
            name = multilingualService.lookupPhrase("statistics_result_all", languageCode);
        }
        return name;
    }

    private Double calculateTotalGrowthIndex(final List<StatisticsCatalyst> catalysts) {
        final List<StatisticsCatalyst> enabledCatalysts = catalysts.stream()
                .filter(catalyst -> catalyst.getIndex() > 0)
                .collect(toList());

        final double ratio = enabledCatalysts.size() > 0 ? enabledCatalysts.size() : 1d;

        return catalysts.stream().mapToDouble(StatisticsCatalyst::getWeighedIndex).sum() / ratio;
    }

    @NonNull
    @Transactional(readOnly = true)
    public SurveyStatistics calculateStatistics(@NonNull final OrganizationSurvey survey,
                                                @NonNull final Map<QuestionId, QuestionStatistics> questionStatistics) {

        final List<DriverStatistics> driverStatistics = calculateDriversStatistics(survey, questionStatistics);
        final List<CatalystStatistics> catalystsStatistics = calculateCatalystsStatistics(survey, driverStatistics, questionStatistics);
        final Double totalGrowthIndex = calculateTotalResult(catalystsStatistics);

        return SurveyStatistics.builder()
                .titles(survey.getTitles().getPhrases())
                .catalysts(catalystsStatistics.stream().collect(collectingAndThen(toMap(
                        CatalystStatistics::getId,
                        v -> v,
                        (v1, v2) -> v1
                ), Collections::unmodifiableMap)))
                .totalResult(totalGrowthIndex)
                .build();
    }

    private Statistics calculateStatistics(
            final Survey survey,
            final Set<Question> questions,
            final FiltersDto filters,
            final String languageCode
    ) {
        // TODO currently always using default survey. Change when necessary (ie. iCan or something like that)
        filters.setSurveyId(survey.getId());

        final Segment segmentFromFilter = getSegmentFromFilter(filters);

        final List<Respondent> respondents = respondentService.getUniqueRespondentsInternally(filters);
        // find all related answers by respondents
        final Map<Question, StatisticsAnswer> answerStatistics = getAnswerStatistics(questions, respondents);
        final List<StatisticsQuestion> questionStatistics = getQuestionStatistic(answerStatistics, respondents);

        final List<Segment> segments = getSegmentsForRespondents(respondents);
        final Map<Long, String> catalystAndDriverNames = getCatalystAndDriversNames(segmentFromFilter, segments, languageCode);

        final Customer customer = getCustomerForRespondents(respondents);

        final List<StatisticsDriver> driverStatistics = calculateDriversStatistics(customer, answerStatistics, catalystAndDriverNames);
        final List<StatisticsCatalyst> catalystsStatistics = calculateCatalystsStatistics(questionStatistics, driverStatistics, catalystAndDriverNames);
        final Double totalGrowthIndex = calculateTotalGrowthIndex(catalystsStatistics);

        // survey title is set in survey object
        final String surveyTitle = multilingualService.lookupPhrase(survey.getTitleId(), languageCode);

        // report "name" will be either static text (filtered/all) or individual user names
        String name = getName(respondents, filters, languageCode);

        // report "customerName" will be either customer name(s) or empty if none can be deducted
        final String customerName = getCustomerNames(filters);

        // report "companyName" will be either respondent group name(s) or empty if none can be deducted
        final String companyName = getRespondentGroupNames(filters);

        return Statistics.builder()
                .respondents(respondents)
                .catalysts(catalystsStatistics)
                .totalGrowthIndex(totalGrowthIndex)
                .respondentCount((long) respondents.size())
                .surveyTitle(surveyTitle)
                .customerName(customerName)
                .companyName(companyName)
                .name(name)
                .questionsRank(StatisticsQuestionsRank.builder().questionStatistics(questionStatistics).build())
                .build();
    }

    public List<StatisticsQuestion> getQuestionStatistic(final Map<Question, StatisticsAnswer> answerStatistics, final List<Respondent> respondents) {
        if (respondents.size() > 4) {
            return answerStatistics.entrySet().stream()
                    .map(e -> StatisticsQuestion.builder()
                            .titleId(e.getKey().getTitleId())
                            .catalystId(e.getKey().getCatalystId())
                            .answer(e.getValue())
                            .build())
                    .sorted(QUESTION_COMPARATOR.reversed())
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        } else {
            return ImmutableList.of();
        }
    }

    private List<StatisticsCatalyst> calculateCatalystsStatistics(final List<StatisticsQuestion> questionsStatistics, final List<StatisticsDriver> driverStatistics, final Map<Long, String> catalystsNames) {
        final Double allDriverWeightSum = driverStatistics.stream().mapToDouble(StatisticsDriver::getWeight).sum();

        final ImmutableList.Builder<StatisticsCatalyst> builder = ImmutableList.builder();
        questionService.getAllCatalysts().forEach(catalyst -> {
            final List<Long> driverIds = questionService.getAllCatalystDrivers(catalyst).stream().map(DriverDto::getId).collect(toList());
            final List<StatisticsDriver> catalystDriversStatistics = driverStatistics.stream().filter(e -> driverIds.contains(e.getId())).collect(collectingAndThen(toList(), Collections::unmodifiableList));

            final Double catalystWeight = catalystDriversStatistics.stream().mapToDouble(StatisticsDriver::getWeight).sum() / allDriverWeightSum;
            final Double catalystResult = catalystDriversStatistics.stream().mapToDouble(e -> e.getResult() != null ? e.getResult() : 0d).sum() / catalystDriversStatistics.size();
            final Double catalystWeighedResult = catalystDriversStatistics.stream().mapToDouble(e -> e.getWeighedResult() != null ? e.getWeighedResult() : 0d).sum() / catalystDriversStatistics.size();
            final List<StatisticsQuestion> catalystQuestionsStatistics = questionsStatistics.stream()
                    .filter(e -> e.getCatalystId().equals(catalyst.getOldId()))
                    .sorted(QUESTION_COMPARATOR.reversed())
                    .collect(collectingAndThen(toList(), Collections::unmodifiableList));

            builder.add(StatisticsCatalyst.builder()
                    .id(catalyst.getOldId())
                    .pdfName(catalyst.getPdfName())
                    .title(catalystsNames.get(catalyst.getOldId()))
                    .developmentTrackIndices(catalystDriversStatistics)
                    .questions(catalystQuestionsStatistics)
                    .result(catalystResult)
                    .weighedResult(catalystWeighedResult)
                    .index(catalystResult) //TODO: remove
                    .weighedIndex(catalystWeighedResult) //TODO: remove
                    .weight(catalystWeight)
                    .build());
        });

        final List<StatisticsCatalyst> catalysts = builder.build();

        final DescriptiveStatistics statistics = new DescriptiveStatistics(catalysts.stream().mapToDouble(StatisticsCatalyst::getWeight).toArray());
        final Double catalystWeightMax = statistics.getMax();

        catalysts.forEach(e -> {
            e.setImportance(e.getWeight() / catalystWeightMax);
        });

        return catalysts;
    }

    @NonNull
    private List<DriverDto> getAllDrivers(@Nullable final Customer customer) {
        if (customer == null) {
            log.info("Getting all drivers.");
            return questionService.getAllDrivers();
        } else {
            log.info("Getting drivers for customer: {}", customer.getName());
            return questionService.getAllDrivers(customer);
        }
    }

    private List<StatisticsDriver> calculateDriversStatistics(final Customer customer, final Map<Question, StatisticsAnswer> questionStatistics, final Map<Long, String> driverNames) {
        final Map<Long, StatisticsDriver> driversStatistics = Maps.newHashMap();
        final Map<Long, Double> driverWeights = Maps.newHashMap();
        final Map<Long, Double> sumOfQuestionsWeightsPerDriver = Maps.newHashMap();
        final Map<Long, Double> sumOfQuestionsResultsPerDriver = Maps.newHashMap();
        final Map<Long, Double> sumOfQuestionsWeighedResultsPerDriver = Maps.newHashMap();

        final List<DriverDto> drivers = getAllDrivers(customer);

        // Prepare temporary maps
        drivers.forEach(e -> {
            driversStatistics.put(e.getId(), StatisticsDriver.builder()
                    .id(e.getId())
                    .title(driverNames.get(e.getId()))
                    .pdfName(e.getPdfName())
                    .catalystId(e.getCatalystId())
                    .build());
            driverWeights.put(e.getId(), e.getWeight());

            sumOfQuestionsWeightsPerDriver.put(e.getId(), 0d);
            sumOfQuestionsResultsPerDriver.put(e.getId(), 0d);
            sumOfQuestionsWeighedResultsPerDriver.put(e.getId(), 0d);
        });

        calculateDriverWeightModifier(driversStatistics, driverWeights);

        // Sums all question results and weights per driver to calculate weighted average after all
        for (final Map.Entry<Question, StatisticsAnswer> entry : questionStatistics.entrySet()) {

            entry.getKey().getWeights().forEach(e -> {
                final Long driverId = e.getQuestionGroupId();
                final Double questionWeight = e.getWeight();
                final Double questionAverage = entry.getValue().getAvg();
                sumOfQuestionsWeightsPerDriver.computeIfPresent(driverId, (k, v) -> v + questionWeight);

                if (questionAverage != null) { // may happen if there are no answers for the question
                    final Double driverWeightModifier = driversStatistics.get(driverId).getWeightModifier();
                    final Double questionResult = questionAverage / MAX_ANSWER_VALUE;

                    sumOfQuestionsResultsPerDriver.computeIfPresent(driverId, (k, v) -> v + (questionWeight * questionResult));
                    sumOfQuestionsWeighedResultsPerDriver.computeIfPresent(driverId, (k, v) -> v + (questionWeight * calculateWeighedQuestionResult(questionResult, driverWeightModifier)));
                }
            });
        }

        // Just calculates weighted average for each driver
        for (final Map.Entry<Long, StatisticsDriver> entry : driversStatistics.entrySet()) {
            final Double driverQuestionWeightsSum = sumOfQuestionsWeightsPerDriver.get(entry.getKey());
            final Double driverQuestionResultsSum = sumOfQuestionsResultsPerDriver.get(entry.getKey());
            final Double driverQuestionWeighedResultSum = sumOfQuestionsWeighedResultsPerDriver.get(entry.getKey());

            if (driverQuestionWeightsSum > 0d) {
                entry.getValue().setResult(driverQuestionResultsSum / driverQuestionWeightsSum);
                entry.getValue().setWeighedResult(driverQuestionWeighedResultSum / driverQuestionWeightsSum);
            }
        }

        return ImmutableList.copyOf(driversStatistics.values());
    }

    private void calculateDriverWeightModifier(final Map<Long, StatisticsDriver> driversStatistics, final Map<Long, Double> driverWeights) {

        final double[] weights = driverWeights.values().stream().mapToDouble(e -> e != null ? e : DEFAULT_WEIGHT_VALUE).sorted().toArray();

        final DescriptiveStatistics statistics = new DescriptiveStatistics(weights);
        final Double driverWeightMedian = statistics.getPercentile(MEDIAN_PERCENTILE_VALUE);
        final Double driverWeightSum = statistics.getSum();
        final Double driverWeightMax = statistics.getMax();

        for(final Map.Entry<Long, StatisticsDriver> entry : driversStatistics.entrySet()) {
            final Double driverWeight = Optional.ofNullable(driverWeights.get(entry.getKey())).orElse(DEFAULT_WEIGHT_VALUE);

            entry.getValue().setWeight(driverWeight / driverWeightSum);
            entry.getValue().setWeightModifier(driverWeight / driverWeightMedian);
            entry.getValue().setImportance(driverWeight / driverWeightMax);
        }
    }

    private Double calculateWeighedQuestionResult(final Double questionAverage, final Double driverWeightModifier) {
        // Average is from 0 to 1, but for 0 we want to have like min 20% so we must normalize the result
        return NORMALIZED_RESULT_RANGE * Math.pow(questionAverage, driverWeightModifier) + NORMALIZED_MIN_ANSWER_VALUE;
    }

    private int getUniqueCount(List list) {
        if (list == null) {
            return 0;
        }
        return (int) list.stream().distinct().count();
    }

    private List<Statistics> calculateComparativeStatisticForCustomers(final Survey survey, final Set<Question> questions, final List<UUID> customerIds, final FiltersDto filters, final String languageCode) {
        final ImmutableList.Builder<Statistics> builder = ImmutableList.builder();


        if (customerIds != null) {
            List<Customer> customers = customerRepository.findAllById(customerIds);

            for(Customer customer : customers) {
                // find respondents based on customer id

                FiltersDto newFilters = new FiltersDto(filters);
                newFilters.setCustomerIds(Collections.singletonList(customer.getId()));

                builder.add(calculateStatistics(survey, questions, newFilters, languageCode));
            }
        }

        return builder.build();
    }

    private List<Statistics> calculateComparativeStatisticForRespondentGroups(final Survey survey, final Set<Question> questions, final List<String> respondentGroupIds, final FiltersDto filters, final String languageCode) {
        final ImmutableList.Builder<Statistics> builder = ImmutableList.builder();

        if (respondentGroupIds != null) {
            List<RespondentGroup> respondentGroups = respondentGroupRepository.findAllById(respondentGroupIds);
            for(RespondentGroup respondentGroup : respondentGroups) {
                // find respondents based on respondent group id

                FiltersDto newFilters = new FiltersDto(filters);
                newFilters.setRespondentGroupIds(Collections.singletonList(respondentGroup.getId()));

                builder.add(calculateStatistics(survey, questions, newFilters, languageCode));
            }
        }

        return builder.build();
    }

    private List<Statistics> calculateComparativeStatisticForRespondents(final Survey survey, final Set<Question> questions, final List<String> respondentIds, final String languageCode) {
        final ImmutableList.Builder<Statistics> builder = ImmutableList.builder();

        if (respondentIds != null) {
            for(String respondentId : respondentIds) {
                // find respondents based on respondent id
                // for respondents we discard all other filters and only use ids
                FiltersDto newFilters = new FiltersDto();
                newFilters.setRespondentIds(Collections.singletonList(respondentId));

                builder.add(calculateStatistics(survey, questions, newFilters, languageCode));
            }
        }

        return builder.build();
    }
}
