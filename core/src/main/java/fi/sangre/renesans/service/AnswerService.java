package fi.sangre.renesans.service;


import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Service
public class AnswerService {
    private final AnswerDao answerDao;

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<CatalystId, OpenQuestionAnswer>> getCatalystsQuestionsAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of catalyst open questions answers for respondent(id={})", respondentId);
        return new AsyncResult<>(answerDao.getCatalystsQuestionsAnswers(surveyId, respondentId)
                .stream()
                .collect(collectingAndThen(toMap(OpenQuestionAnswer::getCatalystId, e -> e), Collections::unmodifiableMap)));
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<CatalystId, List<LikertQuestionAnswer>>> getQuestionsAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of likert questions answers for respondent(id={})", respondentId);
        return new AsyncResult<>(answerDao.getQuestionsAnswers(surveyId, respondentId)
                .stream()
                .collect(groupingBy(LikertQuestionAnswer::getCatalystId)));
    }

}

