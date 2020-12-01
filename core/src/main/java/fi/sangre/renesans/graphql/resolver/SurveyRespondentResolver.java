package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.RespondentOutput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class SurveyRespondentResolver implements GraphQLResolver<RespondentOutput> {
    @NonNull
    public UUID getId(@NonNull final RespondentOutput output) {
        return output.getId().getValue();
    }
}
