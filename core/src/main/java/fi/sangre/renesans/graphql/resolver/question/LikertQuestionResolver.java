package fi.sangre.renesans.graphql.resolver.question;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.graphql.output.question.QuestionDriverWeights;
import fi.sangre.renesans.graphql.resolver.MultilingualTextResolver;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
@Slf4j

@Component
public class LikertQuestionResolver implements GraphQLResolver<LikertQuestion> {
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getId(@NonNull final LikertQuestion output) {
        return output.getId().asString();
    }

    @NonNull
    public String getTitle(@NonNull final LikertQuestion output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public QuestionDriverWeights getWeights(@NonNull final LikertQuestion output) {
        return new QuestionDriverWeights(Optional.ofNullable(output.getWeights())
                .orElse(ImmutableMap.of()).entrySet().stream()
                .collect(collectingAndThen(toMap(
                        e -> e.getKey().asString(),
                        Map.Entry::getValue
                ), Collections::unmodifiableMap)));
    }
}
