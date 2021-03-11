package fi.sangre.renesans.graphql.assemble.questionnaire;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.CatalystId;
import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.application.model.answer.LikertQuestionAnswer;
import fi.sangre.renesans.application.model.answer.OpenQuestionAnswer;
import fi.sangre.renesans.application.utils.CatalystUtils;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import fi.sangre.renesans.graphql.output.QuestionnaireDriverOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireOpenQuestionOutput;
import fi.sangre.renesans.graphql.output.question.QuestionnaireQuestionOutput;
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
public class QuestionnaireCatalystAssembler {
    private final QuestionnaireLikertQuestionAssembler questionnaireLikertQuestionAssembler;
    private final QuestionnaireOpenQuestionAssembler questionnaireOpenQuestionAssembler;
    private final CatalystUtils catalystUtils;

    @NonNull
    public List<QuestionnaireCatalystOutput> from(@NonNull final List<Catalyst> catalysts,
                                                  @NonNull final Map<CatalystId, List<OpenQuestionAnswer>> openQuestionAnswers,
                                                  @NonNull final Map<CatalystId, List<LikertQuestionAnswer>> questionAnswers) {
        return catalysts.stream()
                .map(e -> from(e,
                        openQuestionAnswers.getOrDefault(e.getId(), ImmutableList.of()),
                        questionAnswers.getOrDefault(e.getId(), ImmutableList.of())))
                .filter(catalystUtils::hasQuestions)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<QuestionnaireCatalystOutput> from(@NonNull final List<Catalyst> catalysts) {
        return catalysts.stream()
                .map(this::from)
                .filter(catalystUtils::hasQuestions)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    private QuestionnaireCatalystOutput from(@NonNull final Catalyst catalyst) {
        final List<QuestionnaireLikertQuestionOutput> questions = questionnaireLikertQuestionAssembler.from(catalyst.getQuestions());
        final List<QuestionnaireOpenQuestionOutput> openQuestions = questionnaireOpenQuestionAssembler.from(catalyst.getOpenQuestions());

        return QuestionnaireCatalystOutput.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles())
                .descriptions(catalyst.getDescriptions())
                .drivers(fromDrivers(catalyst.getDrivers()))
                .questions(questions)
                .openQuestions(openQuestions)
                .allAnswered(false)
                .build();
    }

    @NonNull
    private QuestionnaireCatalystOutput from(@NonNull final Catalyst catalyst,
                                             @NonNull final List<OpenQuestionAnswer> openAnswers,
                                             @NonNull final List<LikertQuestionAnswer> likertAnswers) {
        final List<QuestionnaireLikertQuestionOutput> questions = questionnaireLikertQuestionAssembler.from(catalyst.getQuestions(), likertAnswers);
        final List<QuestionnaireOpenQuestionOutput> openQuestions = questionnaireOpenQuestionAssembler.from(catalyst.getOpenQuestions(), openAnswers);

        // Take only likert questions into account as answering open questions is not required
        final boolean allAnswered = questions.stream()
                .map(this::isAnsweredOrSkipped)
                .reduce(Boolean.TRUE, Boolean::logicalAnd);

        return QuestionnaireCatalystOutput.builder()
                .id(catalyst.getId().getValue())
                .titles(catalyst.getTitles())
                .descriptions(catalyst.getDescriptions())
                .drivers(fromDrivers(catalyst.getDrivers()))
                .questions(questions)
                .openQuestions(openQuestions)
                .allAnswered(allAnswered)
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

    private boolean isAnsweredOrSkipped(@Nullable final QuestionnaireQuestionOutput question) {
        if (question == null) {
            return true;
        } else {
            return question.isAnswered() || question.isSkipped();
        }
    }
}
