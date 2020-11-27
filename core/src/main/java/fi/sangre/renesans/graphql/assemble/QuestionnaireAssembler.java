package fi.sangre.renesans.graphql.assemble;

import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.Respondent;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireDriverOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireQuestionOutput;
import fi.sangre.renesans.service.OrganizationSurveyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireAssembler {
    private static final Map<QuestionId, Object> EMPTY_ANSWERS = ImmutableMap.of();

    private final OrganizationSurveyService organizationSurveyService;

    @NonNull
    public QuestionnaireOutput from(@NonNull final OrganizationSurvey survey) {
        return builder(survey)
                .catalysts(fromCatalysts(survey.getCatalysts()))
                .build();
    }

    @NonNull
    public QuestionnaireOutput from(@NonNull final Respondent respondent) {
        final OrganizationSurvey survey = organizationSurveyService.getSurvey(respondent.getSurveyId());
        final QuestionnaireOutput.QuestionnaireOutputBuilder builder = builder(survey)
                .id(respondent.getId().getValue()) // overwrite the id with the respondend one
                .answerable(true);

        final Map<Long, Map<QuestionId, Object>> answers = ImmutableMap.of(); //TODO: get list of answers

        builder.catalysts(fromCatalysts(survey.getCatalysts(), answers));

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
    private List<QuestionnaireCatalystOutput> fromCatalysts(@NonNull final List<Catalyst> catalysts, @NonNull final Map<Long, Map<QuestionId, Object>> answers) {
        return catalysts.stream()
                .map(e -> from(e, answers.getOrDefault(e.getId(), EMPTY_ANSWERS)))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireCatalystOutput from(@NonNull final Catalyst catalyst) {
        return QuestionnaireCatalystOutput.builder()
                .id(catalyst.getId())
                .titles(catalyst.getTitles())
                .drivers(fromDrivers(catalyst.getDrivers()))
                .questions(fromQuestions(catalyst.getQuestions()))
                .build();
    }

    @NonNull
    private QuestionnaireCatalystOutput from(@NonNull final Catalyst catalyst, @NonNull final Map<QuestionId, Object> answers) {
        return QuestionnaireCatalystOutput.builder()
                .id(catalyst.getId())
                .titles(catalyst.getTitles())
                .drivers(fromDrivers(catalyst.getDrivers()))
                .questions(fromQuestions(catalyst.getQuestions(), answers))
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

    @NonNull
    private List<QuestionnaireQuestionOutput> fromQuestions(@NonNull final List<LikertQuestion> questions) {
        return questions.stream()
                .map(question -> QuestionnaireLikertQuestionOutput.builder()
                        .id(question.getId())
                        .titles(question.getTitles().getPhrases())
                        .answered(false)
                        .skipped(false)
                        .index(null)
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private List<QuestionnaireQuestionOutput> fromQuestions(@NonNull final List<LikertQuestion> questions, @NonNull final Map<QuestionId, Object> answers) {
        return questions.stream()
                .map(question -> from(question, answers.get(question.getId())))
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireQuestionOutput from(@NonNull final LikertQuestion question, @Nullable final Object answer) {
        if (answer != null) {
            return QuestionnaireLikertQuestionOutput.builder()
                    .id(question.getId())
                    .titles(question.getTitles().getPhrases())
                    .answered(false) // TODO: answer.isAnswered
                    .skipped(false)  // TODO: answer.isSkipped
                    .index(null)
                    .build();
        } else {
            return QuestionnaireLikertQuestionOutput.builder()
                    .id(question.getId())
                    .titles(question.getTitles().getPhrases())
                    .answered(false)
                    .skipped(false)
                    .index(null)
                    .build();
        }
    }
}
