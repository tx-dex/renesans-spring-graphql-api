package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.parameter.RespondentParameterAnswerOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentParameterAnswerResolver implements GraphQLResolver<RespondentParameterAnswerOutput> {
    @NonNull
    @Deprecated
    public UUID getId(@NonNull final RespondentParameterAnswerOutput output) {
        return output.getRootId().getValue();
    }

    @NonNull
    public UUID getRootId(@NonNull final RespondentParameterAnswerOutput output) {
        return output.getRootId().getValue();
    }
}
