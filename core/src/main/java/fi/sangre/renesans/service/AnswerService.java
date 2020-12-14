package fi.sangre.renesans.service;


import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.event.RespondentAnswerEvent;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.application.utils.SurveyUtils;
import fi.sangre.renesans.exception.InternalServiceException;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import static fi.sangre.renesans.config.ApplicationConfig.ASYNC_EXECUTOR_NAME;
import static fi.sangre.renesans.config.ApplicationConfig.DAO_EXECUTOR_NAME;
import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Service
public class AnswerService {
    private final AnswerDao answerDao;
    private final RespondentDao respondentDao;
    private final SurveyDao surveyDao;
    private final SurveyUtils surveyUtils;
    private final ApplicationEventPublisher applicationEventPublisher;

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<CatalystId, OpenQuestionAnswer>> getCatalystsQuestionsAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of catalyst open questions answers for respondent(id={}, survey_id={})", respondentId, surveyId);

        return new AsyncResult<>(answerDao.getCatalystsQuestionsAnswers(surveyId, respondentId)
                .stream()
                .collect(collectingAndThen(toMap(OpenQuestionAnswer::getCatalystId, e -> e), Collections::unmodifiableMap)));
    }

    public void answerQuestion(@NonNull final OpenQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        try {
            answerDao.saveAnswer(answer, surveyId, respondentId);
            applicationEventPublisher.publishEvent(new RespondentAnswerEvent(surveyId, respondentId));
        } catch (final Exception ex) {
            log.warn("Cannot answer open question respondent(id={})", respondentId, ex);
            throw new InternalServiceException("Cannot answer");
        }
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<CatalystId, List<LikertQuestionAnswer>>> getQuestionsAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of likert questions answers for respondent(id={}, survey_id={})", respondentId, surveyId);

        return new AsyncResult<>(answerDao.getQuestionsAnswers(surveyId, respondentId)
                .stream()
                .collect(groupingBy(LikertQuestionAnswer::getCatalystId)));
    }

    public void answerQuestion(@NonNull final LikertQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        try {
            answerDao.saveAnswer(answer, surveyId, respondentId);
            applicationEventPublisher.publishEvent(new RespondentAnswerEvent(surveyId, respondentId));
        } catch (final Exception ex) {
            log.warn("Cannot answer likert question respondent(id={})", respondentId, ex);
            throw new InternalServiceException("Cannot answer");
        }
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Collection<Respondent>> getRespondentsParametersAnswersAsync(@NonNull final SurveyId surveyId) {
        log.debug("Getting list of parameters answers for survey(id={})", surveyId);

        return new AsyncResult<>(answerDao.getParametersAnswers(surveyId));
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<ParameterId, ParameterItemAnswer>> getParametersAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of parameters answers for respondent(id={}, surveyId={})", respondentId, surveyId);

        return new AsyncResult<>(answerDao.getParametersAnswers(surveyId, respondentId)
                .stream()  // Map with only ParameterId key can be used as respondent is unique and can belong only to one survey
                .collect(collectingAndThen(toMap(e -> e.getRootId().getParameterId(), e -> e), Collections::unmodifiableMap)));
    }

    public void answerParameter(@NonNull final Parameter answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        try {
            answerDao.saveAnswer(answer, surveyId, respondentId);
            applicationEventPublisher.publishEvent(new RespondentAnswerEvent(surveyId, respondentId));
        } catch (final Exception ex) {
            log.warn("Cannot answer parameter respondent(id={})", respondentId, ex);
            throw new InternalServiceException("Cannot answer");
        }
    }

    @EventListener
    @Async(ASYNC_EXECUTOR_NAME)
    public void handleAnswerEvent(@NonNull final RespondentAnswerEvent event) {
        final RespondentId respondentId = event.getRespondentId();
        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(event.getSurveyId());
        final long questionsCount = surveyUtils.countLikertQuestions(survey);
        final long answeredCount = answerDao.countRespondentAnswers(event.getSurveyId(), respondentId);
        final boolean answeredAll = questionsCount == answeredCount;

        if (answeredAll) {
            respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.ANSWERED);
        } else if (!respondentDao.isAnswering(respondentId)) {
            respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.ANSWERING);
        }
    }
}

