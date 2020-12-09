package fi.sangre.renesans.graphql.assemble;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.graphql.output.question.QuestionnaireOpenQuestionOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireOpenQuestionAssembler {
    @Nullable
    public QuestionnaireOpenQuestionOutput from(@NonNull final Catalyst catalyst, @Nullable final OpenQuestionAnswer answer) {
        if (catalyst.getOpenQuestion() != null) {
            final QuestionnaireOpenQuestionOutput.QuestionnaireOpenQuestionOutputBuilder output = QuestionnaireOpenQuestionOutput.builder()
                    .id(new QuestionId(catalyst.getId().getValue()))
                    .titles(catalyst.getOpenQuestion().getPhrases())
                    .answered(false)
                    .skipped(false)
                    .response(null);

            if (answer != null) {
                output.skipped(AnswerStatus.SKIPPED.equals(answer.getStatus()));

                if (AnswerStatus.ANSWERED.equals(answer.getStatus())) {
                    output.answered(true)
                            .response(answer.getResponse());
                }
            }

            return output.build();
        } else {
            return null;
        }
    }
}
