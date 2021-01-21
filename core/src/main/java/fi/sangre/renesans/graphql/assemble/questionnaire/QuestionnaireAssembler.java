package fi.sangre.renesans.graphql.assemble.questionnaire;

import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.graphql.assemble.SurveyMediaAssembler;
import fi.sangre.renesans.graphql.assemble.media.MediaDetailsAssembler;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireAssembler {
    private final OrganizationSurveyService organizationSurveyService;
    private final RespondentDao respondentDao;
    private final AnswerService answerService;
    private final QuestionnaireCatalystAssembler questionnaireCatalystAssembler;
    private final QuestionnaireParameterOutputAssembler questionnaireParameterOutputAssembler;
    private final SurveyMediaAssembler surveyMediaAssembler;
    private final MediaDetailsAssembler mediaDetailsAssembler;

    @NonNull
    public QuestionnaireOutput from(@NonNull final OrganizationSurvey survey) {
        return builder(survey)
                .catalysts(questionnaireCatalystAssembler.from(survey.getCatalysts()))
                .parameters(questionnaireParameterOutputAssembler.from(survey.getParameters()))
                .build();
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final RespondentId respondentId, @NonNull final SurveyId surveyId) throws InterruptedException, ExecutionException {
        return from(respondentId, organizationSurveyService.getSurvey(surveyId));
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final RespondentId respondentId, @NonNull final OrganizationSurvey survey) throws InterruptedException, ExecutionException {
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Future<Map<CatalystId, List<LikertQuestionAnswer>>> questionsAnswers = answerService.getQuestionsAnswersAsync(surveyId, respondentId);
        final Future<Map<CatalystId, OpenQuestionAnswer>> catalystAnswers = answerService.getCatalystsQuestionsAnswersAsync(surveyId, respondentId);
        final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(surveyId, respondentId);

        return from(respondentId, survey, catalystAnswers.get(), questionsAnswers.get(), parameterAnswers.get());
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final RespondentId respondentId,
                                    @NonNull final OrganizationSurvey survey,
                                    @NonNull final Map<CatalystId, OpenQuestionAnswer> openQuestionAnswers,
                                    @NonNull final Map<CatalystId, List<LikertQuestionAnswer>> questionsAnswers,
                                    @NonNull final Map<ParameterId, ParameterItemAnswer> parameterAnswers) {

        final boolean isConsented = respondentDao.isConsented(respondentId);
        final List<QuestionnaireCatalystOutput> catalysts = questionnaireCatalystAssembler.from(survey.getCatalysts(), openQuestionAnswers, questionsAnswers);
        final boolean isAfterGameEnabled = SurveyState.AFTER_GAME.equals(survey.getState()); //TODO: get from survey
        final boolean isAllAnswered = catalysts.stream()
                .map(QuestionnaireCatalystOutput::isAllAnswered)
                .reduce(Boolean.TRUE, Boolean::logicalAnd);

        return builder(survey)
                .id(respondentId.getValue()) // overwrite the id with the respondent one
                .consented(isConsented)
                .answerable(!isAfterGameEnabled)
                .finished(isAllAnswered)
                .canAnswer(!isAfterGameEnabled)
                .canComment(isAfterGameEnabled && isAllAnswered)
                .canViewAfterGame(isAfterGameEnabled && isAllAnswered)
                .catalysts(catalysts)
                .parameters(questionnaireParameterOutputAssembler.from(survey.getParameters(), parameterAnswers))
                .build();
    }

    private QuestionnaireOutput.QuestionnaireOutputBuilder builder(@NonNull final OrganizationSurvey survey) {
        return QuestionnaireOutput.builder()
                .id(survey.getId())
                .logo(mediaDetailsAssembler.from(survey.getLogo()))
                .media(surveyMediaAssembler.from(survey.getMedia()))
                .staticTexts(survey.getStaticTexts())
                .consented(false)
                .finished(true)
                .answerable(false)
                .canAnswer(false)
                .canComment(false)
                .canViewAfterGame(true);

    }
}
