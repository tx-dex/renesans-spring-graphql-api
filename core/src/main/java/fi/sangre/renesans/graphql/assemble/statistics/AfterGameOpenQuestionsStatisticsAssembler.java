package fi.sangre.renesans.graphql.assemble.statistics;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOpenQuestionAnswerOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOpenQuestionStatisticsOutput;
import fi.sangre.renesans.graphql.output.statistics.AfterGameQuestionStatisticsOutput;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

import static fi.sangre.renesans.application.utils.StatisticsUtils.indexToRate;
import static fi.sangre.renesans.application.utils.StatisticsUtils.rateToPercent;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameOpenQuestionsStatisticsAssembler {

    @NonNull
    public Collection<AfterGameOpenQuestionStatisticsOutput> from(@NonNull final List<CatalystOpenQuestionAnswerEntity> answers, @NonNull OrganizationSurvey survey) {
        Collection<AfterGameOpenQuestionStatisticsOutput> outputs = new ArrayList<>();

        survey.getCatalysts().stream().flatMap(s -> s.getOpenQuestions().stream()).forEach(openQuestion -> {
            List<CatalystOpenQuestionAnswerEntity> questionAnswers = answers.stream().filter(answer -> answer.getId().getQuestionId().equals(openQuestion.getId().getValue())).collect(Collectors.toList());

            if(!questionAnswers.isEmpty()) {
                Map<String, String> catalystTitles = survey.getCatalysts().stream()
                        .filter(c -> c.getId().equals(openQuestion.getCatalystId()))
                        .findFirst()
                        .map(value -> value.getTitles().getPhrases())
                        .orElse(null);

                List<AfterGameOpenQuestionAnswerOutput> answerOutputs = questionAnswers.stream().map(answer ->
                    AfterGameOpenQuestionAnswerOutput.builder()
                            .answer(answer.getResponse())
                            .isPublic(answer.isPublic())
                            .build()).collect(Collectors.toList());

                outputs.add(AfterGameOpenQuestionStatisticsOutput.builder()
                        .questionId(openQuestion.getId().getValue())
                        .titles(openQuestion.getTitles().getPhrases())
                        .catalystTitles(catalystTitles)
                        .answers(answerOutputs)
                        .build());
            }
        });

        return outputs;
    }
}
