package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.question.QuestionnaireLikertQuestionOutput;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class QuestionnaireLikertQuestionResolver implements GraphQLResolver<QuestionnaireLikertQuestionOutput> {
    private final MetadataLanguageHelper metadataLanguageHelper;
    private final ResolverHelper resolverHelper;

    @NonNull
    public String getId(@NonNull final QuestionnaireLikertQuestionOutput output) {
        return output.getId().getValue().toString();
    }

    @NonNull
    public String getTitle(@NonNull final QuestionnaireLikertQuestionOutput output, @NonNull final DataFetchingEnvironment environment) {
        return metadataLanguageHelper.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }
}
