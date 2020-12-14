package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.MultilingualText;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.answer.AnswerStatus;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.graphql.input.answer.CatalystOpenQuestionAnswerInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@Slf4j

@Component
public class OpenAnswerAssembler {
    private final SurveyUtils surveyUtils;

    @NonNull
    public OpenQuestionAnswer from(@NonNull CatalystOpenQuestionAnswerInput answer, @NonNull final OrganizationSurvey survey) {
        final CatalystId catalystId = new CatalystId(Objects.requireNonNull(answer.getCatalystId(), "answer.catalystId is required"));

        final MultilingualText question = Optional.ofNullable(surveyUtils.findOpenQuestion(catalystId, survey))
                .orElseThrow(() -> new SurveyException("Open question not found"));

        final OpenQuestionAnswer.OpenQuestionAnswerBuilder builder = OpenQuestionAnswer.builder()
                .id(new QuestionId(answer.getCatalystId()))
                .catalystId(catalystId)
                .status(AnswerStatus.SKIPPED);

        if (StringUtils.isNotBlank(answer.getResponse())) {
            builder.response(answer.getResponse())
                    .status(AnswerStatus.ANSWERED);
        }

        return builder.build();
    }
}
