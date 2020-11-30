package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireDriverOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.service.AnswerService;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireAssembler {
    private final OrganizationSurveyService organizationSurveyService;
    private final AnswerService answerService;
    private final QuestionnaireLikertQuestionAssembler questionnaireLikertQuestionAssembler;
    private final QuestionnaireOpenQuestionAssembler questionnaireOpenQuestionAssembler;
    private final QuestionnaireParameterOutputAssembler questionnaireParameterOutputAssembler;

    @NonNull
    public QuestionnaireOutput from(@NonNull final OrganizationSurvey survey) {
        return builder(survey)
                .catalysts(fromCatalysts(survey.getCatalysts()))
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

        final QuestionnaireOutput.QuestionnaireOutputBuilder builder = builder(survey)
                .id(respondentId.getValue()) // overwrite the id with the respondent one
                .answerable(true);


        builder.catalysts(fromCatalysts(survey.getCatalysts(), catalystAnswers.get(), questionsAnswers.get()));

        return builder.build();
    }

    private QuestionnaireOutput.QuestionnaireOutputBuilder builder(@NonNull final OrganizationSurvey survey) {
        return QuestionnaireOutput.builder()
                .id(survey.getId())
                .staticTexts(survey.getStaticTexts())
                .finished(true)
                .answerable(false);
    }

    @NonNull
    private List<QuestionnaireCatalystOutput> fromCatalysts(@NonNull final List<Catalyst> catalysts) {
        return catalysts.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private List<QuestionnaireCatalystOutput> fromCatalysts(@NonNull final List<Catalyst> catalysts,
                                                            @NonNull final Map<CatalystId, OpenQuestionAnswer> catalystAnswers,
                                                            @NonNull final Map<CatalystId, List<LikertQuestionAnswer>> questionAnswers) {
        return catalysts.stream()
                .map(e -> from(e, catalystAnswers.get(e.getId()), questionAnswers.getOrDefault(e.getId(), ImmutableList.of())))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireCatalystOutput from(@NonNull final Catalyst catalyst) {
        return QuestionnaireCatalystOutput.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles())
                .drivers(fromDrivers(catalyst.getDrivers()))
                .questions(questionnaireLikertQuestionAssembler.from(catalyst.getQuestions()))
                .catalystQuestion(questionnaireOpenQuestionAssembler.from(catalyst, null))
                .build();
    }

    @NonNull
    private QuestionnaireCatalystOutput from(@NonNull final Catalyst catalyst,
                                             @Nullable final OpenQuestionAnswer catalystAnswer,
                                             @NonNull final List<LikertQuestionAnswer> questionAnswers) {
        return QuestionnaireCatalystOutput.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles())
                .drivers(fromDrivers(catalyst.getDrivers()))
                .questions(questionnaireLikertQuestionAssembler.from(catalyst.getQuestions(), questionAnswers))
                .catalystQuestion(questionnaireOpenQuestionAssembler.from(catalyst, catalystAnswer))
                .build();
    }

    @NonNull
    private List<QuestionnaireDriverOutput> fromDrivers(@NonNull final List<Driver> drivers) {
        return drivers.stream()
                .map(driver -> QuestionnaireDriverOutput.builder()
                        .id(driver.getId())
                        .titles(driver.getTitles())
                        .descriptions(driver.getDescriptions())
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
