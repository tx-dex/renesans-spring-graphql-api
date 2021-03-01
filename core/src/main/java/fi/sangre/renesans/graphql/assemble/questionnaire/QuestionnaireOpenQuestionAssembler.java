package fi.sangre.renesans.graphql.assemble.questionnaire;

import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.graphql.output.question.QuestionnaireOpenQuestionOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireOpenQuestionAssembler {
    @NonNull
    public List<QuestionnaireOpenQuestionOutput> from(@NonNull final List<OpenQuestion> questions) {
        return questions.stream()
                .map(e -> from(e, null))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<QuestionnaireOpenQuestionOutput> from(@NonNull final List<OpenQuestion> questions,
                                                      @NonNull final List<OpenQuestionAnswer> answers) {
        return from(questions, (Map<QuestionId, OpenQuestionAnswer>) answers.stream()
                .collect(collectingAndThen(toMap(OpenQuestionAnswer::getId, e -> e), Collections::unmodifiableMap))
        );
    }

    @NonNull
    private List<QuestionnaireOpenQuestionOutput> from(@NonNull final List<OpenQuestion> questions, @NonNull final Map<QuestionId, OpenQuestionAnswer> answers) {
        return questions.stream()
                .map(question -> from(question, answers.get(question.getId())))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireOpenQuestionOutput from(@NonNull final OpenQuestion question,
                                                 @Nullable final OpenQuestionAnswer answer) {
        final QuestionnaireOpenQuestionOutput.QuestionnaireOpenQuestionOutputBuilder output = QuestionnaireOpenQuestionOutput.builder()
                .id(question.getId())
                .titles(question.getTitles().getPhrases())
                .answered(false)
                .skipped(false)
                .response(null);

        if (answer != null) {
            output.skipped(AnswerStatus.SKIPPED.equals(answer.getStatus()))
                    .isPublic(answer.isPublic());

            if (AnswerStatus.ANSWERED.equals(answer.getStatus())) {
                output.answered(true)
                        .response(answer.getResponse());
            }
        }

        return output.build();
    }
}
