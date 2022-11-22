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
import fi.sangre.renesans.application.utils.MultilingualUtils;
import fi.sangre.renesans.graphql.output.statistics.SurveyCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyDriverStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyOpenQuestionStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.SurveyQuestionStatisticsOutput;
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
public class SurveyCatalystStatisticsAssembler {
    private final static List<String> EMPTY = ImmutableList.of();
    private final MultilingualUtils multilingualUtils;

    @NonNull
    public List<SurveyCatalystStatisticsOutput> from(@NonNull final OrganizationSurvey survey, @Nullable final SurveyResult statistics, @NonNull final String languageTag) {
        final Map<CatalystId, CatalystStatistics> catalystStatistics = Optional.ofNullable(statistics)
                .map(SurveyResult::getStatistics)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());

        return survey.getCatalysts().stream()
                .map(catalyst -> from(catalyst, catalystStatistics, languageTag))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public SurveyCatalystStatisticsOutput from(@NonNull final Catalyst catalyst, @Nullable final SurveyResult statistics, @NonNull final String languageTag) {
        final Map<CatalystId, CatalystStatistics> catalystStatistics = Optional.ofNullable(statistics)
                .map(SurveyResult::getStatistics)
                .map(SurveyStatistics::getCatalysts)
                .orElse(ImmutableMap.of());

        return from(catalyst, catalystStatistics, languageTag);
    }

    @NonNull
    private SurveyCatalystStatisticsOutput from(@NonNull final Catalyst catalyst,
                                                @NonNull final Map<CatalystId, CatalystStatistics> statistics,
                                                @NonNull final String languageTag) {
        final CatalystStatistics catalystStatistics = statistics.getOrDefault(catalyst.getId(), CatalystStatistics.EMPTY);

        return SurveyCatalystStatisticsOutput.builder()
                .id(catalyst.getId().asString())
                .title(MultilingualUtils.getText(catalyst.getTitles().getPhrases(), languageTag))
                .result(rateToPercent(catalystStatistics.getWeighedResult()))
                .drivers(catalyst.getDrivers().stream()
                        .map(driver -> from(driver, catalystStatistics.getDrivers(), languageTag))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .questions(catalyst.getQuestions().stream()
                        .map(question -> from(question, catalystStatistics.getQuestions(), languageTag))
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                .openQuestions(catalyst.getOpenQuestions().stream()
                        .map(question -> from(question, ImmutableMap.of(), languageTag)) //TODO: get answers
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))

                .build();
    }

    @NonNull
    private SurveyDriverStatisticsOutput from(@NonNull final Driver driver, @NonNull final Map<DriverId, DriverStatistics> statistics, @NonNull final String languageTag) {
        final DriverId driverId = new DriverId(driver.getId());
        final DriverStatistics driverStatistics = statistics.getOrDefault(driverId, DriverStatistics.EMPTY);

        return SurveyDriverStatisticsOutput.builder()
                .id(driver.getId().toString())
                .title(MultilingualUtils.getText(driver.getTitles().getPhrases(), languageTag))
                .result(rateToPercent(driverStatistics.getWeighedResult()))
                .build();
    }

    @NonNull
    private SurveyQuestionStatisticsOutput from(@NonNull final LikertQuestion question,
                                                @NonNull final Map<QuestionId, QuestionStatistics> statistics,
                                                @NonNull final String languageTag) {
        final QuestionStatistics questionStatistics = statistics.getOrDefault(question.getId(), QuestionStatistics.EMPTY);

        return SurveyQuestionStatisticsOutput.builder()
                .id(question.getId().getValue())
                .title(MultilingualUtils.getText(question.getTitles().getPhrases(), languageTag))
                .result(rateToPercent(questionStatistics.getAvg()))
                .rate(questionStatistics.getRate())
                .participants(questionStatistics.getCount())
                .skipped(questionStatistics.getSkipped())
                .build();
    }

    @NonNull
    private SurveyOpenQuestionStatisticsOutput from(@NonNull final OpenQuestion question,
                                                    @NonNull final Map<QuestionId, List<String>> answers,
                                                    @NonNull final String languageTag) {
        return SurveyOpenQuestionStatisticsOutput.builder()
                .id(question.getId().getValue())
                .title(MultilingualUtils.getText(question.getTitles().getPhrases(), languageTag))
                .answers(answers.getOrDefault(question.getId(), EMPTY))
                .build();
    }
}
