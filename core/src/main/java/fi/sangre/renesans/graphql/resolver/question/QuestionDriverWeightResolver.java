package fi.sangre.renesans.graphql.resolver.question;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.DriverWeight;
import fi.sangre.renesans.graphql.output.DriverProxy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionDriverWeightResolver implements GraphQLResolver<DriverWeight> {
    @NonNull
    public DriverProxy getDriver(@NonNull final DriverWeight output) {
        return DriverProxy.toProxy(output.getDriver());
    }
}
