package fi.sangre.renesans.graphql.assemble.statistics;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.graphql.assemble.OpenQuestionAnswerAssembler;
import fi.sangre.renesans.graphql.output.statistics.AfterGameOpenQuestionStatisticsOutput;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameOpenQuestionsStatisticsAssembler {

    final private OpenQuestionAnswerAssembler openQuestionAnswerAssembler;

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

                outputs.add(AfterGameOpenQuestionStatisticsOutput.builder()
                        .questionId(openQuestion.getId().getValue())
                        .titles(openQuestion.getTitles().getPhrases())
                        .catalystTitles(catalystTitles)
                        .answers(openQuestionAnswerAssembler.from(questionAnswers))
                        .build());
            }
        });

        return outputs;
    }
}
