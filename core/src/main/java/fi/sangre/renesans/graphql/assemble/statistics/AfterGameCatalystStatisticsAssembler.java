package fi.sangre.renesans.graphql.assemble.statistics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.CatalystStatistics;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameDriverStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOpenQuestionOutput;
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

import static fi.sangre.renesans.application.utils.StatisticsUtils.rateToPercent;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameCatalystStatisticsAssembler {
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
        final CatalystStatistics respondentCatalyst = respondentCatalysts.getOrDefault(catalyst.getId(), CatalystStatistics.EMPTY);
        final CatalystStatistics respondentGroupCatalyst = respondentGroupCatalysts.getOrDefault(catalyst.getId(), CatalystStatistics.EMPTY);

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
                .openQuestion(Optional.ofNullable(catalyst.getOpenQuestion())
                        .map(question -> AfterGameOpenQuestionOutput.builder()
                                .titles(question.getPhrases())
                        .answers(ImmutableList.of())
                        .build())
                .orElse(null))
                .build();
    }

    @NonNull
    private AfterGameDriverStatisticsOutput from(@NonNull final Driver driver,
                                                 @NonNull final Map<DriverId, DriverStatistics> respondentDrivers,
                                                 @NonNull final Map<DriverId, DriverStatistics> respondentGroupDrivers) {
        final DriverId driverId = new DriverId(driver.getId());
        final DriverStatistics respondentDriver = respondentDrivers.getOrDefault(driverId, DriverStatistics.EMPTY);
        final DriverStatistics respondentGroupDriver = respondentGroupDrivers.getOrDefault(driverId, DriverStatistics.EMPTY);

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
        final QuestionStatistics respondentQuestion = respondentQuestions.getOrDefault(question.getId(), QuestionStatistics.EMPTY);
        final QuestionStatistics respondentGroupQuestion = respondentGroupQuestions.getOrDefault(question.getId(), QuestionStatistics.EMPTY);

        return AfterGameQuestionStatisticsOutput.builder()
                .titles(question.getTitles().getPhrases())
                .result(rateToPercent(respondentGroupQuestion.getAvg()))
                .rate(rateToPercent(respondentGroupQuestion.getRate()))
                .build();
    }
}