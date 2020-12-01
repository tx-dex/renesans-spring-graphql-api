package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.answer.ParameterItemAnswer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentParameterAnswerResolver implements GraphQLResolver<ParameterItemAnswer> {

    @NonNull
    public UUID getId(@NonNull final ParameterItemAnswer output) {
        return output.getRootId().getParameterId().getValue();
    }

    @NonNull
    public String getResponse(@NonNull final ParameterItemAnswer output) {
        return "empty"; //TODO: implement
    }
}
