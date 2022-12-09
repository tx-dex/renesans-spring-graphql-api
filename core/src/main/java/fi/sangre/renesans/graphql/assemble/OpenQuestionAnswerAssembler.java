package fi.sangre.renesans.graphql.assemble;

import fi.sangre.renesans.graphql.output.statistics.OpenQuestionAnswerOutput;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class OpenQuestionAnswerAssembler {

    @NonNull
    public List<OpenQuestionAnswerOutput> from(@NonNull final List<CatalystOpenQuestionAnswerEntity> answers) {
        return answers.stream().map(answer ->
                OpenQuestionAnswerOutput.builder()
                        .answer(answer.getResponse())
                        .isPublic(answer.isPublic())
                        .build()).collect(Collectors.toList());
    }
}
