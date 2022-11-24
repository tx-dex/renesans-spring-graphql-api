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

import static fi.sangre.renesans.application.utils.StatisticsUtils.indexToRate;
import static fi.sangre.renesans.application.utils.StatisticsUtils.rateToPercent;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameOpenQuestionsStatisticsAssembler {
    private final SurveyUtils surveyUtils;

    @NonNull
    public Collection<AfterGameOpenQuestionStatisticsOutput> from(@NonNull final List<CatalystOpenQuestionAnswerEntity> answers, @NonNull OrganizationSurvey survey) {
        Collection<AfterGameOpenQuestionStatisticsOutput> outputs = new ArrayList<>();

        answers.forEach(answer -> {
            AfterGameOpenQuestionAnswerOutput answerOutput = AfterGameOpenQuestionAnswerOutput.builder()
                    .answer(answer.getResponse())
                    .isPublic(answer.isPublic())
                    .build();

            Optional<AfterGameOpenQuestionStatisticsOutput> openQuestionStatisticsOutput = outputs.stream().filter(output -> output.getQuestionId().equals(answer.getId().getQuestionId())).findFirst();

            if(openQuestionStatisticsOutput.isPresent()) {
                openQuestionStatisticsOutput.get().addAnswer(answerOutput);
            } else {
                OpenQuestion question = surveyUtils.findOpenQuestion(new QuestionId(answer.getId().getQuestionId()), survey);

                if (question == null) {
                    throw new SurveyException("Couldn't find a question by ID");
                }

                Collection<AfterGameOpenQuestionAnswerOutput> answerOutputs = new ArrayList<>();
                answerOutputs.add(answerOutput);

                Map<String, String> catalystTitles = survey.getCatalysts().stream()
                        .filter(c -> c.getId().equals(question.getCatalystId()))
                        .findFirst()
                        .map(value -> value.getTitles().getPhrases())
                        .orElse(null);

                outputs.add(AfterGameOpenQuestionStatisticsOutput.builder()
                                .questionId(answer.getId().getQuestionId())
                                .titles(question.getTitles().getPhrases())
                                .catalystTitles(catalystTitles)
                                .answers(answerOutputs)
                                .build());
            }
        });

        return outputs;
    }
}
