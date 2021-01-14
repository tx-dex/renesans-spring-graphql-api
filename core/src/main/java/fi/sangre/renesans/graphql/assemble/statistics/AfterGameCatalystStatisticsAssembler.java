package fi.sangre.renesans.graphql.assemble.statistics;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.CatalystStatistics;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameDriverStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameQuestionStatisticsOutput;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameCatalystStatisticsAssembler {
    private static final CatalystStatistics EMPTY_CATALYST = CatalystStatistics.builder()
            .weighedResult(null)
            .drivers(ImmutableMap.of())
            .questions(ImmutableMap.of())
            .build();
    private static final DriverStatistics EMPTY_DRIVER = DriverStatistics.builder()
            .weighedResult(null)
            .build();
    private static final QuestionStatistics EMPTY_QUESTION = QuestionStatistics.builder()
            .avg(null)
            .rate(null)
            .build();

    @NonNull
    public List<AfterGameCatalystStatisticsOutput> from(@NonNull final OrganizationSurvey survey,
                                                        @Nullable final SurveyStatistics respondentResult,
                                                        @Nullable final SurveyStatistics respondentGroupResult) {

        final Map<CatalystId, CatalystStatistics> respondentCatalysts = Optional.ofNullable(respondentResult)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());
        final Map<CatalystId, CatalystStatistics> respondentGroupCatalysts = Optional.ofNullable(respondentGroupResult)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());

        return survey.getCatalysts().stream()
                .map(catalyst -> from(catalyst, respondentCatalysts, respondentGroupCatalysts))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public AfterGameCatalystStatisticsOutput from(@NonNull final Catalyst catalyst,
                                                  @Nullable final SurveyStatistics respondentResult,
                                                  @Nullable final SurveyStatistics respondentGroupResult) {
        final Map<CatalystId, CatalystStatistics> respondentCatalysts = Optional.ofNullable(respondentResult)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());
        final Map<CatalystId, CatalystStatistics> respondentGroupCatalysts = Optional.ofNullable(respondentGroupResult)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());

        return from(catalyst, respondentCatalysts, respondentGroupCatalysts);
    }

    @NonNull
    private AfterGameCatalystStatisticsOutput from(@NonNull final Catalyst catalyst,
                                                   @NonNull final Map<CatalystId, CatalystStatistics> respondentCatalysts,
                                                   @NonNull final Map<CatalystId, CatalystStatistics> respondentGroupCatalysts) {
        final CatalystStatistics respondentCatalyst = respondentCatalysts.getOrDefault(catalyst.getId(), EMPTY_CATALYST);
        final CatalystStatistics respondentGroupCatalyst = respondentGroupCatalysts.getOrDefault(catalyst.getId(), EMPTY_CATALYST);

        return AfterGameCatalystStatisticsOutput.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles().getPhrases())
                .respondentResult(rateToPercent(respondentCatalyst.getWeighedResult()))
                .respondentGroupResult(rateToPercent(respondentGroupCatalyst.getWeighedResult()))
                .drivers(catalyst.getDrivers().stream()
                        .map(driver -> from(driver,
                                respondentCatalyst.getDrivers(),
                                respondentGroupCatalyst.getDrivers()))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .questions(catalyst.getQuestions().stream()
                        .map((question -> from(question,
                                respondentCatalyst.getQuestions(),
                                respondentGroupCatalyst.getQuestions())))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .openQuestion(null)
                .build();
    }

    @NonNull
    private AfterGameDriverStatisticsOutput from(@NonNull final Driver driver,
                                                 @NonNull final Map<DriverId, DriverStatistics> respondentDrivers,
                                                 @NonNull final Map<DriverId, DriverStatistics> respondentGroupDrivers) {
        final DriverId driverId = new DriverId(driver.getId());
        final DriverStatistics respondentDriver = respondentDrivers.getOrDefault(driverId, EMPTY_DRIVER);
        final DriverStatistics respondentGroupDriver = respondentGroupDrivers.getOrDefault(driverId, EMPTY_DRIVER);

        return AfterGameDriverStatisticsOutput.builder()
                .titles(driver.getTitles().getPhrases())
                .respondentResult(rateToPercent(respondentDriver.getWeighedResult()))
                .respondentGroupResult(rateToPercent(respondentGroupDriver.getWeighedResult()))
                .build();
    }

    @NonNull
    private AfterGameQuestionStatisticsOutput from(@NonNull final LikertQuestion question,
                                                   @NonNull final Map<QuestionId, QuestionStatistics> respondentQuestions,
                                                   @NonNull final Map<QuestionId, QuestionStatistics> respondentGroupQuestions) {
        final QuestionStatistics respondentQuestion = respondentQuestions.getOrDefault(question.getId(), EMPTY_QUESTION);
        final QuestionStatistics respondentGroupQuestion = respondentGroupQuestions.getOrDefault(question.getId(), EMPTY_QUESTION);

        return AfterGameQuestionStatisticsOutput.builder()
                .titles(question.getTitles().getPhrases())
                .result(rateToPercent(respondentGroupQuestion.getAvg()))
                .rate(rateToPercent(respondentGroupQuestion.getRate()))
                .build();
    }

    @Nullable
    public Double rateToPercent(@Nullable final Double result) {
        if (result != null) {
            return 100 * result;
        } else {
            return null;
        }
    }
}