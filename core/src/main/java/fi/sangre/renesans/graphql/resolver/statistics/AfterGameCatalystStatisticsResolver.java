package fi.sangre.renesans.graphql.resolver.statistics;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.graphql.output.statistics.AfterGameCatalystStatisticsOutput;
import fi.sangre.renesans.graphql.resolver.MultilingualTextResolver;
import fi.sangre.renesans.graphql.resolver.ResolverHelper;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j

@Component
public class AfterGameCatalystStatisticsResolver implements GraphQLResolver<AfterGameCatalystStatisticsOutput> {
    private final ResolverHelper resolverHelper;
    private final MultilingualTextResolver multilingualTextResolver;

    @NonNull
    public String getTitle(@NonNull final AfterGameCatalystStatisticsOutput output, @NonNull final DataFetchingEnvironment environment) {
        return multilingualTextResolver.getRequiredText(output.getTitles(), resolverHelper.getLanguageCode(environment));
    }
}
