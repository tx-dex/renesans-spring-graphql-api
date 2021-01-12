package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyUtils {

    public long countLikertQuestions(@NonNull final OrganizationSurvey survey) {
        return survey.getCatalysts().stream()
                .map(catalyst -> catalyst.getQuestions().size())
                .mapToLong(Integer::longValue).sum();
    }

    @Nullable
    public MultilingualText findOpenQuestion(@NonNull final CatalystId id, @NonNull final OrganizationSurvey survey) {
        return Optional.ofNullable(survey.getCatalysts())
                .orElse(ImmutableList.of())
                .stream()
                .filter(e -> e.getId().equals(id))
                .map(Catalyst::getOpenQuestion)
                .filter(Objects::nonNull)
                .filter(e -> !e.isEmpty())
                .findAny()
                .orElse(null);
    }

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

    @Nullable
    public Parameter findParameter(@NonNull final ParameterId parameterId, @NonNull final OrganizationSurvey survey) {
        return Optional.ofNullable(survey.getParameters())
                .orElse(ImmutableList.of())
                .stream()
                .filter(parameter -> parameterId.equals(parameter.getId()))
                .findFirst().orElse(null);
    }

    @Nullable
    public Parameter findParameter(@NonNull final UUID parameterId, @NonNull final OrganizationSurvey survey) {
        return findParameter(new ParameterId(parameterId), survey);
    }

    @Nullable
    public Parameter findParameter(@NonNull final String parameterId, @NonNull final OrganizationSurvey survey) {
        return findParameter(UUID.fromString(parameterId), survey);
    }
}
