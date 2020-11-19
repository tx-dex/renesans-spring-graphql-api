package fi.sangre.renesans.graphql.resolver.question;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.DriverWeight;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.graphql.resolver.MetadataLanguageHelper;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Slf4j

@Component
public class LikertQuestionResolver implements GraphQLResolver<LikertQuestion> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getId(@NonNull final LikertQuestion output) {
        return output.getId().getValue().toString();
    }

    @NonNull
    public String getTitle(@NonNull final LikertQuestion output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getTitle().getPhrases(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public List<DriverWeight> getWeights(@NonNull final LikertQuestion output) {
        return ImmutableList.of();
    }
}
