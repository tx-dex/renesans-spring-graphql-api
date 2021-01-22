package fi.sangre.renesans.graphql.resolver.discussion;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.Lists;
import fi.sangre.renesans.graphql.output.discussion.AfterGameCommentOutput;
import fi.sangre.renesans.graphql.output.discussion.AfterGameDiscussionOutput;
import fi.sangre.renesans.graphql.resolver.MultilingualTextResolver;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameDiscussionResolver implements GraphQLResolver<AfterGameDiscussionOutput> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getTitle(@NonNull final AfterGameDiscussionOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public Collection<AfterGameCommentOutput> getComments(@NonNull final AfterGameDiscussionOutput output, @Nullable final Integer size) {
        final List<AfterGameCommentOutput> comments;
        if (size != null) {
            comments = output.getComments().stream()
                    .limit(size)
                    .collect(toList());
        } else {
            comments = output.getComments();
        }

        return Collections.unmodifiableList(Lists.reverse(comments));
    }
}
