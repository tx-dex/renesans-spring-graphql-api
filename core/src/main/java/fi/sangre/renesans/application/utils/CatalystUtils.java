package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class CatalystUtils {
    public boolean hasQuestions(@NonNull final Catalyst catalyst) {
        final boolean hasLikertQuestions = catalyst.getQuestions() != null && catalyst.getQuestions().size() > 0;
        final boolean hasOpenQuestion = catalyst.getOpenQuestion() != null && !catalyst.getOpenQuestion().isEmpty();

        return hasLikertQuestions || hasOpenQuestion;
    }

    public boolean hasQuestions(@NonNull final QuestionnaireCatalystOutput catalyst) {
        final boolean hasLikertQuestions = catalyst.getQuestions() != null && catalyst.getQuestions().size() > 0;
        final boolean hasOpenQuestion = catalyst.getCatalystQuestion() != null;

        return hasLikertQuestions || hasOpenQuestion;
    }
}
