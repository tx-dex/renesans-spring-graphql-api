package fi.sangre.renesans.application.utils;

import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.OrganizationSurvey;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyUtils {
    @Nullable
    public LikertQuestion findQuestion(@NonNull final QuestionId questionId, @NonNull final OrganizationSurvey survey) {
        for(final Catalyst catalyst : survey.getCatalysts()) {
            final LikertQuestion question = catalyst.getQuestions().stream()
                    .filter(e -> questionId.equals(e.getId()))
                    .findFirst()
                    .orElse(null);

            if (question != null) {
                question.setCatalystId(catalyst.getId());

                return question;
            }
        }

        return null;
    }
}
