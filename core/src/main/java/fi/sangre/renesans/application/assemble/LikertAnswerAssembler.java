package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionAnswerInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class LikertAnswerAssembler {
    private final SurveyUtils surveyUtils;

    @NonNull
    public LikertQuestionAnswer from(@NonNull LikertQuestionAnswerInput answer, @NonNull final OrganizationSurvey survey) {
        final QuestionId questionId = new QuestionId(Objects.requireNonNull(answer.getQuestionId(), "answer.questionId is required"));

        final LikertQuestion question = Optional.ofNullable(surveyUtils.findQuestion(questionId, survey))
                .orElseThrow(() -> new SurveyException("Question not found in the questionnaire"));

        final LikertQuestionAnswer.LikertQuestionAnswerBuilder builder = LikertQuestionAnswer.builder()
                .id(questionId)
                .catalystId(question.getCatalystId())
                .status(AnswerStatus.SKIPPED);

        if (answer.getResponse() != null) {
            if (answer.getResponse() < 0 || answer.getResponse() > 4) {
                throw new RuntimeException("Invalid 'answer.response' value. Must be 0, 1, 2, 3 or 4");
            }

            builder.response(answer.getResponse())
                    .status(AnswerStatus.ANSWERED);
        }

        return builder.build();
    }
}
