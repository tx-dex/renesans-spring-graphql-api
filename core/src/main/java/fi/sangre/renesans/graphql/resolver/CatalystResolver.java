package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.graphql.output.DriverProxy;
import fi.sangre.renesans.graphql.output.OutputProxy;
import fi.sangre.renesans.model.Question;
import fi.sangre.renesans.persistence.model.metadata.CatalystMetadata;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.QuestionService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static fi.sangre.renesans.graphql.output.DriverProxy.toProxies;

@RequiredArgsConstructor

@Component
public class CatalystResolver implements GraphQLResolver<CatalystProxy> {
    private final MultilingualService multilingualService;
    private final QuestionService questionService;
    private final ResolverHelper resolverHelper;
    private final Map<Class<?>, CatalystStrategy<CatalystProxy>> strategies = ImmutableMap.<Class<?>, CatalystStrategy<CatalystProxy>>builder()
            .put(CatalystDto.class, new CatalystDtoStrategy())
            .put(CatalystMetadata.class, new CatalystMetadataStrategy())
            .build();

    @NonNull
    public String getPdfName(@NonNull final CatalystProxy proxy) {
        return proxy.getObject().getPdfName();
    }

    @NonNull
    public String getTitle(@NonNull final CatalystProxy proxy, @NonNull final DataFetchingEnvironment environment) {
        return strategies.get(proxy.getObject().getClass()).getTitle(proxy, resolverHelper.getLanguageCode(environment) );
    }

    @Deprecated
    public Long getTitleId(@NonNull final CatalystProxy proxy) {
        return null;
    }

    @Deprecated
    public String getDescription(final CatalystProxy proxy) {
        return null;
    }

    @NonNull
    public List<Question> getQuestions(final CatalystProxy proxy) {
        return strategies.get(proxy.getObject().getClass()).getQuestions(proxy);
    }

    @NonNull
    public List<DriverProxy> getDrivers(@NonNull final CatalystProxy proxy) {
        return strategies.get(proxy.getObject().getClass()).getDrivers(proxy);
    }

    private interface CatalystStrategy<T extends OutputProxy<?>> {
        @NonNull String getTitle(@NonNull T output, @NonNull String languageTag);
        @NonNull List<DriverProxy> getDrivers(@NonNull T output);
        @NonNull List<Question> getQuestions(@NonNull T output);
    }

    private class CatalystDtoStrategy implements CatalystStrategy<CatalystProxy> {
        @NonNull
        @Override
        public String getTitle(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            final CatalystDto catalyst = (CatalystDto) proxy.getObject();
            return multilingualService.lookupPhrase(catalyst.getTitleId(), languageTag);
        }

        @NonNull
        @Override
        public List<DriverProxy> getDrivers(@NonNull final CatalystProxy proxy) {
            final CatalystDto catalyst = (CatalystDto) proxy.getObject();
            return toProxies(questionService.getAllCatalystDrivers(catalyst));
        }

        @NonNull
        @Override
        public List<Question> getQuestions(@NonNull final CatalystProxy proxy) {
            final CatalystDto catalyst = (CatalystDto) proxy.getObject();
            return questionService.getAllCatalystQuestions(catalyst.getId(), catalyst.getCustomer(), catalyst.getSegment());
        }
    }

    private static class CatalystMetadataStrategy implements CatalystStrategy<CatalystProxy> {
        @NonNull
        @Override
        public String getTitle(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            final CatalystMetadata metadata = (CatalystMetadata) proxy.getObject();
            return metadata.getTitles().get(languageTag);
        }

        @NonNull
        @Override
        public List<DriverProxy> getDrivers(@NonNull final CatalystProxy proxy) {
            final CatalystMetadata metadata = (CatalystMetadata) proxy.getObject();
            return toProxies(metadata.getDrivers());
        }

        @NonNull
        @Override
        public List<Question> getQuestions(@NonNull final CatalystProxy proxy) {
            //TODO: implement
            return ImmutableList.of();
        }
    }
}
