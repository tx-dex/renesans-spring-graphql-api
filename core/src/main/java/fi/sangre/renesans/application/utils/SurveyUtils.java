package fi.sangre.renesans.application.utils;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.*;
import fi.sangre.renesans.application.model.parameter.Parameter;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.application.model.questions.QuestionId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyUtils {
    public List<LikertQuestion> getAllQuestions(@NonNull final OrganizationSurvey survey) {
        return survey.getCatalysts().stream()
                .flatMap(catalyst -> catalyst.getQuestions().stream())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public long countAllQuestions(@NonNull final OrganizationSurvey survey) {
        return countLikertQuestions(survey)
                + countOpenQuestions(survey);
    }

    public long countLikertQuestions(@NonNull final OrganizationSurvey survey) {
        return survey.getCatalysts().stream()
                .map(catalyst -> catalyst.getQuestions().size())
                .mapToLong(Integer::longValue).sum();
    }

    public long countOpenQuestions(@NonNull final OrganizationSurvey survey) {
        return survey.getCatalysts().stream()
                .map(catalyst -> catalyst.getOpenQuestions().size())
                .mapToLong(Integer::longValue).sum();
    }

    public long countParameters(@NonNull final OrganizationSurvey survey) {
        return survey.getParameters().size();
    }

    @Nullable
    public OpenQuestion findOpenQuestion(@NonNull final QuestionId questionId, @NonNull final OrganizationSurvey survey) {
        return Optional.ofNullable(survey.getCatalysts())
                .orElse(ImmutableList.of())
                .stream()
                .flatMap(s -> s.getOpenQuestions().stream())
                .filter(e -> e.getId().equals(questionId))
                .findAny()
                .orElse(null);
    }

    @Nullable
    public LikertQuestion findQuestion(@NonNull final QuestionId questionId, @NonNull final OrganizationSurvey survey) {
        return Optional.ofNullable(survey.getCatalysts())
                .orElse(ImmutableList.of())
                .stream()
                .flatMap(s -> s.getQuestions().stream())
                .filter(e -> e.getId().equals(questionId))
                .findAny()
                .orElse(null);
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

    @Nullable
    public Catalyst findCatalyst(@NonNull final UUID catalystId, @NonNull final OrganizationSurvey survey) {
        return findCatalyst(new CatalystId(catalystId), survey);
    }

    @Nullable
    public Catalyst findCatalyst(@NonNull final CatalystId catalystId, @NonNull final OrganizationSurvey survey) {
        return survey.getCatalysts().stream()
                .filter(catalyst -> catalystId.equals(catalyst.getId()))
                .findFirst()
                .orElse(null);
    }

    @NonNull
    public List<Driver> getAllDrivers(@NonNull final OrganizationSurvey survey) {
        return Optional.ofNullable(survey.getCatalysts())
                .orElse(ImmutableList.of()).stream()
                .flatMap(v -> v.getDrivers().stream())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    public Driver findDriver(@NonNull final Long driverId, @NonNull final OrganizationSurvey survey) {
        return getAllDrivers(survey).stream()
                .filter(driver -> driver.getId().equals(driverId))
                .findFirst().orElseThrow(() -> new RuntimeException("Driver " + driverId + " not found in survey " + survey.getId()));
    }
}
