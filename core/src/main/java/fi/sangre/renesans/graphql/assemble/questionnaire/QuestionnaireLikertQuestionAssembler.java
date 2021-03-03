package fi.sangre.renesans.graphql.assemble.questionnaire;

import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
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
public class QuestionnaireLikertQuestionAssembler {

    @NonNull
    public List<QuestionnaireLikertQuestionOutput> from(@NonNull final List<LikertQuestion> questions) {
        return questions.stream()
                .map(e -> from(e, null))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }


    @NonNull
    public List<QuestionnaireLikertQuestionOutput> from(@NonNull final List<LikertQuestion> questions, @NonNull final List<LikertQuestionAnswer> answers) {
        return from(questions, (Map<QuestionId, LikertQuestionAnswer>) answers.stream()
                .collect(collectingAndThen(toMap(LikertQuestionAnswer::getId, e -> e), Collections::unmodifiableMap))
        );
    }

    @NonNull
    private List<QuestionnaireLikertQuestionOutput> from(@NonNull final List<LikertQuestion> questions, @NonNull final Map<QuestionId, LikertQuestionAnswer> answers) {
        return questions.stream()
                .map(question -> from(question, answers.get(question.getId())))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireLikertQuestionOutput from(@NonNull final LikertQuestion question, @Nullable final LikertQuestionAnswer answer) {
        final QuestionnaireLikertQuestionOutput.QuestionnaireLikertQuestionOutputBuilder output = QuestionnaireLikertQuestionOutput.builder()
                .id(question.getId())
                .titles(question.getTitles().getPhrases())
                .subTitles(question.getSubTitles().getPhrases())
                .lowEndLabels(question.getLowEndLabels().getPhrases())
                .highEndLabels(question.getHighEndLabels().getPhrases())
                .answered(false)
                .skipped(false)
                .response(null);

        if (answer != null) {
            if (AnswerStatus.SKIPPED.equals(answer.getStatus())) {
                output.skipped(true)
                        .response(0);
            }

            if (AnswerStatus.ANSWERED.equals(answer.getStatus())) {
                output
                        .answered(true)
                        .response(answer.getResponse() + 1);
            }

            output.rate(answer.getRate());
        }

        return output.build();
    }
}
