package fi.sangre.renesans.service;


import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.dao.AnswerDao;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.dao.SurveyDao;
import fi.sangre.renesans.application.event.RespondentAnswerEvent;
import fi.sangre.renesans.application.event.RespondentParameterAnswerEvent;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.respondent.GuestId;
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
    public Future<Map<CatalystId, List<OpenQuestionAnswer>>> getOpenQuestionsAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of catalyst open questions answers for respondent(id={}, survey_id={})", respondentId, surveyId);

        return new AsyncResult<>(answerDao.getOpenAnswers(surveyId, respondentId)
                .stream()
                .collect(collectingAndThen(groupingBy(OpenQuestionAnswer::getCatalystId), Collections::unmodifiableMap)));
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
    public Future<Map<CatalystId, List<LikertQuestionAnswer>>> getLikerQuestionsAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of likert questions answers for respondent(id={}, survey_id={})", respondentId, surveyId);

        return new AsyncResult<>(answerDao.getLikertAnswers(surveyId, respondentId)
                .stream()
                .collect(collectingAndThen(groupingBy(LikertQuestionAnswer::getCatalystId), Collections::unmodifiableMap)));
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

    public void rateQuestion(@NonNull final LikertQuestionAnswer answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        try {
            answerDao.saveRating(answer, surveyId, respondentId);
        } catch (final Exception ex) {
            log.warn("Cannot rate likert question respondent(id={})", respondentId, ex);
            throw new InternalServiceException("Cannot rate");
        }
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Collection<Respondent>> getRespondentsParametersAnswersAsync(@NonNull final SurveyId surveyId) {
        return getRespondentsParametersAnswersAsync(surveyId, ImmutableList.of());
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Collection<Respondent>> getRespondentsParametersAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final List<RespondentFilter> filters) {
        log.debug("Getting list of parameters answers for survey(id={})", surveyId);
        return new AsyncResult<>(answerDao.getParametersAnswers(surveyId, filters));
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<ParameterId, ParameterItemAnswer>> getParametersAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        log.debug("Getting list of parameters answers for respondent(id={}, surveyId={})", respondentId, surveyId);

        return new AsyncResult<>(answerDao.getParametersAnswers(surveyId, respondentId)
                .stream()  // Map with only ParameterId key can be used as respondent is unique and can belong only to one survey
                .collect(collectingAndThen(toMap(e -> e.getRootId().getParameterId(), e -> e), Collections::unmodifiableMap)));
    }

    @NonNull
    @Async(DAO_EXECUTOR_NAME)
    public Future<Map<ParameterId, ParameterItemAnswer>> getParametersAnswersAsync(@NonNull final SurveyId surveyId, @NonNull final GuestId guestId) {
        log.debug("Getting list of parameters answers for guest(id={}, surveyId={})", guestId, surveyId);

        return new AsyncResult<>(answerDao.getParametersAnswers(surveyId, guestId)
                .stream()  // Map with only ParameterId key can be used as respondent is unique and can belong only to one survey
                .collect(collectingAndThen(toMap(e -> e.getRootId().getParameterId(), e -> e), Collections::unmodifiableMap)));
    }

    public void answerParameter(@NonNull final Parameter answer, @NonNull final SurveyId surveyId, @NonNull final RespondentId respondentId) {
        try {
            answerDao.saveAnswer(answer, surveyId, respondentId);
            applicationEventPublisher.publishEvent(new RespondentParameterAnswerEvent(surveyId, respondentId));
        } catch (final Exception ex) {
            log.warn("Cannot answer parameter respondent(id={})", respondentId, ex);
            throw new InternalServiceException("Cannot answer");
        }
    }

    public void answerParameter(@NonNull final Parameter answer, @NonNull final SurveyId surveyId, @NonNull final GuestId guestId) {
        try {
            answerDao.saveAnswer(answer, surveyId, guestId);
            applicationEventPublisher.publishEvent(new RespondentParameterAnswerEvent(surveyId, guestId));
        } catch (final Exception ex) {
            log.warn("Cannot answer parameter guest(id={})", guestId, ex);
            throw new InternalServiceException("Cannot answer");
        }
    }


    @EventListener
    @Async(ASYNC_EXECUTOR_NAME)
    public void handleAnswerEvent(@NonNull final RespondentAnswerEvent event) {
        final RespondentId respondentId = event.getRespondentId();
        final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(event.getSurveyId());
        final long questionsCount = surveyUtils.countLikertQuestions(survey);
        final long answeredCount = answerDao.countLikertRespondentAnswers(event.getSurveyId(), respondentId);
        final boolean answeredAll = questionsCount == answeredCount;

        if (answeredAll) {
            respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.ANSWERED);
        } else  {
            respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.ANSWERING);
        }
    }

    @EventListener
    @Async(ASYNC_EXECUTOR_NAME)
    public void handleAnswerEvent(@NonNull final RespondentParameterAnswerEvent event) {
        if (event.getRespondentId() instanceof RespondentId) {
            final RespondentId respondentId = (RespondentId) event.getRespondentId();
            final OrganizationSurvey survey = surveyDao.getSurveyOrThrow(event.getSurveyId());
            final long parametersCount = surveyUtils.countParameters(survey);
            final long answeredCount = answerDao.countAnsweredParameters(event.getSurveyId(), respondentId);
            final boolean answeredAll = parametersCount == answeredCount;

            if (answeredAll) {
                respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.ANSWERED_PARAMETERS);
            } else if (!respondentDao.isAnswering(respondentId)) {
                respondentDao.updateRespondentStatus(respondentId, SurveyRespondentState.ANSWERING_PARAMETERS);
            }
        }
    }
}

