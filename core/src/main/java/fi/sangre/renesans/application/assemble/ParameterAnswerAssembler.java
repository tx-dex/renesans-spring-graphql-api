package fi.sangre.renesans.application.assemble;

import fi.sangre.renesans.application.model.ParameterId;
import fi.sangre.renesans.application.model.QuestionnaireUserParameterId;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import fi.sangre.renesans.application.model.respondent.GuestId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import fi.sangre.renesans.persistence.model.answer.GuestParameterAnswerEntity;
import fi.sangre.renesans.persistence.model.answer.ParameterAnswerEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class ParameterAnswerAssembler {
    @NonNull
    public List<ParameterItemAnswer> fromRespondentAnswers(@NonNull final Collection<ParameterAnswerEntity> answers) {
        return answers.stream()
                .map(e -> ParameterItemAnswer.builder()
                        .rootId(new QuestionnaireUserParameterId(new RespondentId(e.getId().getRespondentId()), new ParameterId(e.getRootId())))
                        .response(new ParameterId(e.getId().getParameterId()))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public List<ParameterItemAnswer> fromGuestAnswers(@NonNull final Collection<GuestParameterAnswerEntity> answers) {
        return answers.stream()
                .map(e -> ParameterItemAnswer.builder()
                        .rootId(new QuestionnaireUserParameterId(new GuestId(e.getId().getGuestId()), new ParameterId(e.getRootId())))
                        .response(new ParameterId(e.getId().getParameterId()))
                        .build())
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

}
