package fi.sangre.renesans.graphql.assemble.statistics;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.statistics.CatalystStatistics;
import fi.sangre.renesans.application.model.statistics.DriverStatistics;
import fi.sangre.renesans.application.model.statistics.SurveyResult;
import fi.sangre.renesans.application.model.statistics.SurveyStatistics;
import fi.sangre.renesans.application.utils.CatalystUtils;
import fi.sangre.renesans.graphql.assemble.OpenQuestionAnswerAssembler;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameDriverStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOpenQuestionOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameQuestionStatisticsOutput;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
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
import static fi.sangre.renesans.application.utils.CatalystUtils.RESPONDENTS_ANSWERED_MINIMUM;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameCatalystStatisticsAssembler {
    private final CatalystUtils catalystUtils;
    private final OpenQuestionAnswerAssembler openQuestionAnswerAssembler;

    @NonNull
    public List<AfterGameCatalystStatisticsOutput> from(@NonNull final OrganizationSurvey survey,
                                                        @Nullable final SurveyResult respondentResult,
                                                        @Nullable final SurveyResult respondentGroupResult,
                                                        @NonNull final Long respondentsAnswered) {

        final Map<CatalystId, CatalystStatistics> respondentCatalysts = Optional.ofNullable(respondentResult)
                .map(SurveyResult::getStatistics)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());
        final Map<CatalystId, CatalystStatistics> respondentGroupCatalysts = Optional.ofNullable(respondentGroupResult)
                .map(SurveyResult::getStatistics)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());

        return survey.getCatalysts().stream()
                .filter(catalystUtils::hasQuestions)
                .map(catalyst -> from(catalyst, respondentCatalysts, respondentGroupCatalysts, respondentsAnswered))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public AfterGameCatalystStatisticsOutput from(@NonNull final Catalyst catalyst,
                                                  @Nullable final SurveyResult respondentResult,
                                                  @Nullable final SurveyResult respondentGroupResult,
                                                  @NonNull final Long respondentsAnswered) {
        final Map<CatalystId, CatalystStatistics> respondentCatalysts = Optional.ofNullable(respondentResult)
                .map(SurveyResult::getStatistics)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());
        final Map<CatalystId, CatalystStatistics> respondentGroupCatalysts = Optional.ofNullable(respondentGroupResult)
                .map(SurveyResult::getStatistics)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());

        return from(catalyst, respondentCatalysts, respondentGroupCatalysts, respondentsAnswered);
    }

    @NonNull
    private AfterGameCatalystStatisticsOutput from(@NonNull final Catalyst catalyst,
                                                   @NonNull final Map<CatalystId, CatalystStatistics> respondentCatalysts,
                                                   @NonNull final Map<CatalystId, CatalystStatistics> respondentGroupCatalysts,
                                                   @NonNull final Long respondentsAnswered) {
        final CatalystStatistics respondentCatalyst = respondentCatalysts.getOrDefault(catalyst.getId(), CatalystStatistics.EMPTY);
        final CatalystStatistics respondentGroupCatalyst = respondentGroupCatalysts.getOrDefault(catalyst.getId(), CatalystStatistics.EMPTY);

        return AfterGameCatalystStatisticsOutput.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles().getPhrases())
                .respondentResult(rateToPercent(respondentCatalyst.getWeighedResult()))
                .respondentGroupResult(respondentsAnswered >= RESPONDENTS_ANSWERED_MINIMUM
                        ? rateToPercent(respondentGroupCatalyst.getWeighedResult())
                        : null
                )
                .drivers(catalyst.getDrivers().stream()
                        .map(driver -> from(driver,
                                respondentCatalyst.getDrivers(),
                                respondentGroupCatalyst.getDrivers(),
                                respondentsAnswered))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .questions(catalyst.getQuestions().stream()
                        .map((question -> from(question,
                                respondentCatalyst.getQuestions(),
                                respondentGroupCatalyst.getQuestions())))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .openQuestions(catalyst.getOpenQuestions().stream()
                        .map((question -> from(question, ImmutableMap.of()))) // TODO: provide this
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .build();
    }


    @NonNull
    private AfterGameOpenQuestionOutput from(@NonNull final OpenQuestion question,
                                             @NonNull final Map<QuestionId, List<CatalystOpenQuestionAnswerEntity>> answers) {
        return AfterGameOpenQuestionOutput.builder()
                .id(question.getId())
                .titles(question.getTitles().getPhrases())
                .answers(openQuestionAnswerAssembler.from(answers.getOrDefault(question.getId(), ImmutableList.of())))
                .build();
    }

    @NonNull
    private AfterGameDriverStatisticsOutput from(@NonNull final Driver driver,
                                                 @NonNull final Map<DriverId, DriverStatistics> respondentDrivers,
                                                 @NonNull final Map<DriverId, DriverStatistics> respondentGroupDrivers,
                                                 @NonNull final Long respondentsAnswered) {
        final DriverId driverId = new DriverId(driver.getId());
        final DriverStatistics respondentDriver = respondentDrivers.getOrDefault(driverId, DriverStatistics.EMPTY);
        final DriverStatistics respondentGroupDriver = respondentGroupDrivers.getOrDefault(driverId, DriverStatistics.EMPTY);

        return AfterGameDriverStatisticsOutput.builder()
                .titles(driver.getTitles().getPhrases())
                .respondentResult(rateToPercent(respondentDriver.getResult()))
                .respondentGroupResult(respondentsAnswered >= RESPONDENTS_ANSWERED_MINIMUM
                        ? rateToPercent(respondentGroupDriver.getResult())
                        : null
                )
                .build();
    }

    @NonNull
    private AfterGameQuestionStatisticsOutput from(@NonNull final LikertQuestion question,
                                                   @NonNull final Map<QuestionId, QuestionStatistics> respondentQuestions,
                                                   @NonNull final Map<QuestionId, QuestionStatistics> respondentGroupQuestions) {
        final QuestionStatistics respondentGroupQuestion = respondentGroupQuestions.getOrDefault(question.getId(), QuestionStatistics.EMPTY);

        return AfterGameQuestionStatisticsOutput.builder()
                .titles(question.getTitles().getPhrases())
                .result(rateToPercent(respondentGroupQuestion.getAvg()))
                .rate(respondentGroupQuestion.getRate())
                .participants(respondentGroupQuestion.getCount())
                .skipped(respondentGroupQuestion.getSkipped())
                .build();
    }
}
