package fi.sangre.renesans.application.dao;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.persistence.model.answer.CatalystOpenQuestionAnswerEntity;
import fi.sangre.renesans.persistence.model.statistics.AnswerDistribution;
import fi.sangre.renesans.persistence.model.statistics.QuestionStatistics;
import fi.sangre.renesans.persistence.repository.CatalystOpenQuestionAnswerRepository;
import fi.sangre.renesans.persistence.repository.LikerQuestionAnswerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class StatisticsDao {
    private final LikerQuestionAnswerRepository likerQuestionAnswerRepository;
    private final CatalystOpenQuestionAnswerRepository catalystOpenQuestionAnswerRepository;

    @NonNull
    @Transactional(readOnly = true)
    public Map<QuestionId, QuestionStatistics> getQuestionStatistics(@NonNull final SurveyId surveyId, @NonNull final Set<RespondentId> respondentIds) {
        if (!respondentIds.isEmpty()) {
            return likerQuestionAnswerRepository.getQuestionStatisticsByQuestionAndRespondentsIn(surveyId.getValue(), RespondentId.toUUIDs(respondentIds)).stream()
                    .collect(collectingAndThen(toMap(
                            v -> new QuestionId(v.getQuestionId()),
                            v -> v,
                            (v1, v2) -> v1
                    ), Collections::unmodifiableMap));
        } else {
            return ImmutableMap.of();
        }
    }

    @NonNull
    @Transactional(readOnly = true)
    public Map<QuestionId, QuestionStatistics> getQuestionStatistics(@NonNull final SurveyId surveyId) {
        return likerQuestionAnswerRepository.getQuestionStatisticsByQuestion(surveyId.getValue()).stream()
                .collect(collectingAndThen(toMap(
                        v -> new QuestionId(v.getQuestionId()),
                        v -> v,
                        (v1, v2) -> v1
                ), Collections::unmodifiableMap));
    }

    @NonNull
    @Transactional(readOnly = true)
    public List<CatalystOpenQuestionAnswerEntity> getOpenQuestionStatistics(@NonNull final SurveyId surveyId, @NonNull final Set<RespondentId> respondentIds, final RespondentId loggedResponderId) {
        List<CatalystOpenQuestionAnswerEntity> answers = new ArrayList<>(catalystOpenQuestionAnswerRepository.findAllByIdSurveyIdAndIsPublicAndIdRespondentIdInOrderByAnswerTimeDesc(surveyId.getValue(), true, RespondentId.toUUIDs(respondentIds)));

        //Add private answers of the logged user if they are part of the respondents
        boolean loggedIsRespondent = respondentIds.stream().anyMatch(respondentId -> respondentId.equals(loggedResponderId));
        if(loggedIsRespondent) {
            answers.addAll(catalystOpenQuestionAnswerRepository.findAllByIdSurveyIdAndIsPublicAndIdRespondentIdInOrderByAnswerTimeDesc(surveyId.getValue(), false, Collections.singleton(loggedResponderId.getValue())));
        }

        return answers;
    }

    public List<AnswerDistribution> getResponseDistributions(@NonNull final UUID questionId) {
        return likerQuestionAnswerRepository.getQuestionResponseDistribution(questionId);
    }

    public List<AnswerDistribution> getRateDistributions(@NonNull final UUID questionId) {
        return likerQuestionAnswerRepository.getQuestionRateDistribution(questionId);
    }
}
