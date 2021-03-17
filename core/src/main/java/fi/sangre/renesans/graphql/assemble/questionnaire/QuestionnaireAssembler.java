package fi.sangre.renesans.graphql.assemble.questionnaire;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.dao.GuestDao;
import fi.sangre.renesans.application.dao.RespondentDao;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.graphql.assemble.SurveyMediaAssembler;
import fi.sangre.renesans.graphql.assemble.media.MediaDetailsAssembler;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.parameter.QuestionnaireParameterOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import fi.sangre.renesans.service.TranslationService;
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
    private final GuestDao guestDao;
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
    public QuestionnaireOutput from(@NonNull final GuestId guestId, @NonNull final SurveyId surveyId) throws InterruptedException, ExecutionException {
        final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(surveyId, guestId);
        final OrganizationSurvey survey = organizationSurveyService.getSurvey(surveyId);

        return from(guestId, survey, parameterAnswers.get());
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final GuestId guestId,
                                     @NonNull final OrganizationSurvey survey) throws InterruptedException, ExecutionException {
        return from(guestId, survey, answerService.getParametersAnswersAsync(new SurveyId(survey.getId()), guestId).get());
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final GuestId guestId,
                                     @NonNull final OrganizationSurvey survey,
                                     @NonNull final Map<ParameterId, ParameterItemAnswer> parameterAnswers) {

        final QuestionnaireUserState state = guestDao.getState(guestId);
        final boolean isConsented = state.isConsented();
        final boolean isViewingAfterGame = state.isViewingAfterGame();
        final List<QuestionnaireCatalystOutput> catalysts = questionnaireCatalystAssembler.from(survey.getCatalysts(), ImmutableMap.of(), ImmutableMap.of());
        final List<QuestionnaireParameterOutput> parameters = questionnaireParameterOutputAssembler.from(survey.getParameters(), parameterAnswers);
        final boolean isAfterGameEnabled = SurveyState.AFTER_GAME.equals(survey.getState());
        final boolean areAllParametersAnswered = parameters.stream()
                .filter(QuestionnaireParameterOutput::isAnswered)
                .count() == parameters.size();

        return builder(survey)
                .id(guestId.getValue()) // overwrite the id with the respondent one
                .selectedLanguage(state.getLanguageTag())
                .consented(isConsented)
                .answerable(!isAfterGameEnabled)
                .finished(true)
                .canAnswerParameters(!isViewingAfterGame)
                .canGoToQuestions(false) //This should be false
                .canViewAfterGame(isAfterGameEnabled && isConsented && areAllParametersAnswered)
                .canAnswer(false)
                .canComment(isAfterGameEnabled)
                .catalysts(catalysts)
                .parameters(parameters)
                .build();
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final RespondentId respondentId, @NonNull final SurveyId surveyId) throws InterruptedException, ExecutionException {
        return from(respondentId, organizationSurveyService.getSurvey(surveyId));
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final RespondentId respondentId, @NonNull final OrganizationSurvey survey) throws InterruptedException, ExecutionException {
        final SurveyId surveyId = new SurveyId(survey.getId());
        final Future<Map<CatalystId, List<LikertQuestionAnswer>>> questionsAnswers = answerService.getLikerQuestionsAnswersAsync(surveyId, respondentId);
        final Future<Map<CatalystId, List<OpenQuestionAnswer>>> catalystAnswers = answerService.getOpenQuestionsAnswersAsync(surveyId, respondentId);
        final Future<Map<ParameterId, ParameterItemAnswer>> parameterAnswers = answerService.getParametersAnswersAsync(surveyId, respondentId);

        return from(respondentId, survey, catalystAnswers.get(), questionsAnswers.get(), parameterAnswers.get());
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final RespondentId respondentId,
                                    @NonNull final OrganizationSurvey survey,
                                    @NonNull final Map<CatalystId, List<OpenQuestionAnswer>> openQuestionAnswers,
                                    @NonNull final Map<CatalystId, List<LikertQuestionAnswer>> questionsAnswers,
                                    @NonNull final Map<ParameterId, ParameterItemAnswer> parameterAnswers) {

        final QuestionnaireUserState state = respondentDao.getState(respondentId);
        final boolean isConsented = state.isConsented();
        final boolean isAnswering = state.isAnsweringQuestions();
        final boolean isViewingAfterGame = state.isViewingAfterGame();
        final List<QuestionnaireCatalystOutput> catalysts = questionnaireCatalystAssembler.from(survey.getCatalysts(), openQuestionAnswers, questionsAnswers);
        final List<QuestionnaireParameterOutput> parameters = questionnaireParameterOutputAssembler.from(survey.getParameters(), parameterAnswers);
        final boolean isAfterGameEnabled = SurveyState.AFTER_GAME.equals(survey.getState()); //TODO: get from survey
        final boolean isAllAnswered = catalysts.stream()
                .map(QuestionnaireCatalystOutput::isAllAnswered)
                .reduce(Boolean.TRUE, Boolean::logicalAnd);
        final boolean areAllParametersAnswered = parameters.stream()
                .filter(QuestionnaireParameterOutput::isAnswered)
                .count() == parameters.size();

        return builder(survey)
                .id(respondentId.getValue()) // overwrite the id with the respondent one
                .selectedLanguage(state.getLanguageTag())
                .consented(isConsented)
                .answerable(!isAfterGameEnabled)
                .finished(isAllAnswered)
                .canAnswerParameters(!isAnswering && !isViewingAfterGame)
                .canGoToQuestions(isConsented && areAllParametersAnswered && !isAfterGameEnabled)
                .canAnswer(!isAfterGameEnabled)
                .canComment(isAfterGameEnabled)
                .canViewAfterGame(isAfterGameEnabled && isConsented && areAllParametersAnswered)
                .catalysts(catalysts)
                .parameters(parameters)
                .build();
    }

    private QuestionnaireOutput.QuestionnaireOutputBuilder builder(@NonNull final OrganizationSurvey survey) {
        return QuestionnaireOutput.builder()
                .id(survey.getId())
                .selectedLanguage(TranslationService.DEFAULT_LANGUAGE)
                .logo(mediaDetailsAssembler.from(survey.getLogo()))
                .media(surveyMediaAssembler.from(survey.getMedia()))
                .staticTexts(survey.getStaticTexts())
                .hideCatalystThemePages(Boolean.TRUE.equals(survey.getHideCatalystThemePages()))
                .consented(false)
                .finished(true)
                .answerable(false)
                .canAnswerParameters(false)
                .canGoToQuestions(true)
                .canAnswer(false)
                .canComment(false)
                .canViewAfterGame(true);

    }
}
