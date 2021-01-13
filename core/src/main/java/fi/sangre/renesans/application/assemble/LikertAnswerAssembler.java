package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionAnswerInput;
import fi.sangre.renesans.graphql.input.answer.LikertQuestionRateInput;
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
    private static final Integer SOME_DIVISOR = 10;
    private final SurveyUtils surveyUtils;


    @NonNull
    public LikertQuestionAnswer from(@NonNull final LikertQuestionRateInput answer, @NonNull final OrganizationSurvey survey) {
        final QuestionId questionId = new QuestionId(Objects.requireNonNull(answer.getQuestionId(), "answer.questionId is required"));

        final LikertQuestion question = Optional.ofNullable(surveyUtils.findQuestion(questionId, survey))
                .orElseThrow(() -> new SurveyException("Question not found in the questionnaire"));

        final LikertQuestionAnswer.LikertQuestionAnswerBuilder builder = LikertQuestionAnswer.builder()
                .id(questionId)
                .catalystId(question.getCatalystId())
                .rate(Math.floorDiv(answer.getRate(), SOME_DIVISOR));  // This is because frontend is giving some strange values like 11, 21, 31, 41, 51

        return builder.build();
    }

    @NonNull
    public LikertQuestionAnswer from(@NonNull final LikertQuestionAnswerInput answer, @NonNull final OrganizationSurvey survey) {
        final QuestionId questionId = new QuestionId(Objects.requireNonNull(answer.getQuestionId(), "answer.questionId is required"));

        final LikertQuestion question = Optional.ofNullable(surveyUtils.findQuestion(questionId, survey))
                .orElseThrow(() -> new SurveyException("Question not found in the questionnaire"));

        final LikertQuestionAnswer.LikertQuestionAnswerBuilder builder = LikertQuestionAnswer.builder()
                .id(questionId)
                .catalystId(question.getCatalystId());

        if (answer.getResponse() != null) {
            if (answer.getResponse() < 0 || answer.getResponse() > 5) {
                throw new RuntimeException("Invalid 'answer.response' value. Must be 0, 1, 2, 3, 4, 5");
            }

            if (answer.getResponse() > 0) {
                builder.response(answer.getResponse() - 1)
                        .status(AnswerStatus.ANSWERED);
            } else {
                builder.response(null)
                        .status(AnswerStatus.SKIPPED);
            }
        } else {
            throw new SurveyException("answer.response must not be null");
        }

        return builder.build();
    }
}
