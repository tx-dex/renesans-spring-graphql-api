package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.graphql.output.QuestionnaireCatalystOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;

@RequiredArgsConstructor
@Slf4j

@Component
public class CatalystUtils {
    public static final Long RESPONDENTS_ANSWERED_MINIMUM = 6L;

    public boolean hasQuestions(@NonNull final Catalyst catalyst) {
        return isNotEmpty(catalyst.getQuestions())
                || isNotEmpty(catalyst.getOpenQuestions());
    }

    public boolean hasQuestions(@NonNull final QuestionnaireCatalystOutput catalyst) {
        return isNotEmpty(catalyst.getQuestions())
                || isNotEmpty(catalyst.getOpenQuestions());
    }

    private <T> boolean isNotEmpty(@Nullable final Collection<T> collection) {
        return collection != null && !collection.isEmpty();
    }
}
