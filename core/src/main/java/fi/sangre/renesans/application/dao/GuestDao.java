package fi.sangre.renesans.application.dao;

import com.google.common.hash.Hashing;
import fi.sangre.renesans.application.model.QuestionnaireUserState;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.utils.RespondentUtils;
import fi.sangre.renesans.exception.ResourceNotFoundException;
import fi.sangre.renesans.exception.SurveyException;
import fi.sangre.renesans.persistence.model.SurveyGuest;
import fi.sangre.renesans.persistence.model.SurveyRespondentState;
import fi.sangre.renesans.persistence.repository.SurveyGuestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class GuestDao {
    private final SurveyGuestRepository surveyGuestRepository;
    private final RespondentUtils respondentUtils;

    @Transactional(readOnly = true)
    public QuestionnaireUserState getState(@NonNull final GuestId id) {
        return getGuest(id, entity -> QuestionnaireUserState.builder()
                .consented(Boolean.TRUE.equals(entity.getConsent()))
                .answeringParameters(respondentUtils.isAnsweringParameters(entity.getState()))
                .answeringQuestions(respondentUtils.isAnsweringQuestions(entity.getState()))
                .viewingAfterGame(respondentUtils.isViewingAfterGame(entity.getState()))
                .build());
    }

    @Transactional
    public void consent(@NonNull final GuestId id, @NonNull final Boolean consent) {
        final SurveyGuest guest = surveyGuestRepository.findById(id.getValue())
                .orElseThrow(() -> new SurveyException("Respondent not found"));

        if (!guest.getConsent().equals(consent)) {
            guest.setConsent(consent);
            surveyGuestRepository.save(guest);
        }
    }

    @Transactional(readOnly = true)
    @NonNull
    public <T> T getGuest(@NonNull final GuestId id, @NonNull final Function<SurveyGuest, T> mapper) {
        return surveyGuestRepository.findById(id.getValue())
                .map(mapper)
                .orElseThrow(() -> new ResourceNotFoundException("Cannot find guest"));
    }

    @Transactional(readOnly = true)
    @Nullable
    public GuestId findGuest(@NonNull final GuestId id, @NonNull final String invitationHash) {
        return surveyGuestRepository.findByIdAndInvitationHash(id.getValue(), invitationHash)
                .map(v -> new GuestId(v.getId()))
                .orElse(null);
    }

    @NonNull
    @Transactional
    public Set<GuestId> registerGuests(@NonNull final SurveyId surveyId, @NonNull final Set<String> emails) {

        final Map<String, SurveyGuest> existing = surveyGuestRepository.findAllBySurveyIdAndEmailIn(surveyId.getValue(), emails)
                .stream()
                .collect(toMap(SurveyGuest::getEmail, v -> v));

        final List<SurveyGuest> toRegister = emails.stream()
                .map(v -> existing.getOrDefault(v, SurveyGuest.builder()
                        .surveyId(surveyId.getValue())
                        .email(v)
                        .state(SurveyRespondentState.INVITING)
                        .consent(false)
                        .build()))
                .map(v -> registerGuest(surveyId, v))
                .collect(Collectors.toList());

        return surveyGuestRepository.saveAll(toRegister).stream()
                .map(v -> new GuestId(v.getId()))
                .collect(collectingAndThen(toSet(), Collections::unmodifiableSet));
    }

    @NonNull
    private SurveyGuest registerGuest(@NonNull final SurveyId surveyId, @NonNull final SurveyGuest newOrExisting) {
        newOrExisting.setInvitationHash(
                Hashing.sha512().hashString(String.format("%s-%s", surveyId.asString(), UUID.randomUUID()), StandardCharsets.UTF_8).toString());

        return newOrExisting;
    }
}
