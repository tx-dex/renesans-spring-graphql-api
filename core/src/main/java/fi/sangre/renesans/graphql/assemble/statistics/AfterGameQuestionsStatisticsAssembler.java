package fi.sangre.renesans.graphql.assemble.statistics;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.output.statistics.AfterGameQuestionStatisticsOutput;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;

import static fi.sangre.renesans.application.utils.StatisticsUtils.rateToPercent;
import static fi.sangre.renesans.application.utils.StatisticsUtils.indexToRate;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameQuestionsStatisticsAssembler {
    private final SurveyUtils surveyUtils;

    @NonNull
    public Collection<AfterGameQuestionStatisticsOutput> from(@NonNull final Map<QuestionId, QuestionStatistics> questions, @NonNull OrganizationSurvey survey) {
        Collection<AfterGameQuestionStatisticsOutput> outputs = new ArrayList<>();

        questions.forEach((questionId, questionStatistics) -> {
            LikertQuestion question = surveyUtils.findQuestion(questionId, survey);

            if (question == null) {
                throw new SurveyException("Couldn't find a question by ID");
            }

            outputs.add(AfterGameQuestionStatisticsOutput.builder()
                    .titles(question.getTitles().getPhrases())
                    .result(rateToPercent(indexToRate(questionStatistics.getAvg())))
                    .rate(questionStatistics.getRate())
                    .skipped(questionStatistics.getSkipped())
                    .build());
        });

        return outputs;
    }
}
