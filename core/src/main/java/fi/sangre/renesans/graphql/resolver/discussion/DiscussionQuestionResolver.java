package fi.sangre.renesans.graphql.resolver.discussion;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.application.model.discussion.DiscussionQuestion;
import fi.sangre.renesans.graphql.resolver.MultilingualTextResolver;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class DiscussionQuestionResolver implements GraphQLResolver<DiscussionQuestion> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;

    @NonNull
    public UUID getId(@NonNull final DiscussionQuestion output) {
        return output.getId().getValue();
    }

    @NonNull
    public String getTitle(@NonNull final DiscussionQuestion output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitle(), resolverHelper.getLanguageCode(environment));
    }
}
