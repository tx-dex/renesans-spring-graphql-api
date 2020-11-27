package fi.sangre.renesans.application.dao;

import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.persistence.model.answer.LikertQuestionAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.QuestionAnswerId;
import fi.sangre.renesans.persistence.repository.CatalystOpenQuestionAnswerRepository;
import fi.sangre.renesans.persistence.repository.LikerQuestionAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toSet;

@RequiredArgsConstructor
@Slf4j

@Component
public class AnswerDao {
    private final CatalystOpenQuestionAnswerRepository catalystOpenQuestionAnswerRepository;
    private final LikerQuestionAnswerRepository likerQuestionAnswerRepository;

    @NonNull
    @Transactional(readOnly = true)
    public Set<OpenQuestionAnswer> getCatalystsQuestionsAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return catalystOpenQuestionAnswerRepository.findAllByIdSurveyIdAndIdRespondentId(surveyId.getValue(), respondentId.getValue())
                .stream()
                .map(e -> OpenQuestionAnswer.builder()
                        .id(new QuestionId(e.getId().getCatalystId()))
                        .catalystId(new CatalystId(e.getId().getCatalystId()))
                        .status(e.getStatus())
                        .response(e.getResponse())
                        .build())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @NonNull
    @Transactional(readOnly = true)
    public Set<LikertQuestionAnswer> getQuestionsAnswers(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        return likerQuestionAnswerRepository.findAllByIdSurveyIdAndIdRespondentId(surveyId.getValue(), respondentId.getValue())
                .stream()
                .map(e -> LikertQuestionAnswer.builder()
                        .id(new QuestionId(e.getId().getQuestionId()))
                        .catalystId(new CatalystId(e.getCatalystId()))
                        .status(e.getStatus())
                        .response(e.getResponse())
                        .build())
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @Transactional
    public void answerQuestion(@NonNull final LikertQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        final QuestionAnswerId id = new QuestionAnswerId(surveyId.getValue(), respondentId.getValue(), answer.getId().getValue());
        final LikertQuestionAnswerEntity entity = likerQuestionAnswerRepository.findById(id)
                .orElse(LikertQuestionAnswerEntity.builder()
                        .id(id)
                        .catalystId(answer.getCatalystId().getValue())
                        .build());

        entity.setResponse(answer.getResponse());
        entity.setStatus(answer.getStatus());

        likerQuestionAnswerRepository.save(entity);
    }
}
