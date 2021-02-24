package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.Catalyst;
import fi.sangre.renesans.application.model.questions.LikertQuestion;
import fi.sangre.renesans.application.model.questions.OpenQuestion;
import fi.sangre.renesans.dto.CatalystDto;
import fi.sangre.renesans.graphql.output.CatalystProxy;
import fi.sangre.renesans.graphql.output.DriverProxy;
import fi.sangre.renesans.graphql.output.OutputProxy;
import fi.sangre.renesans.service.MultilingualService;
import fi.sangre.renesans.service.QuestionService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static fi.sangre.renesans.graphql.output.DriverProxy.toProxies;

@RequiredArgsConstructor

@Component
public class CatalystResolver implements GraphQLResolver<CatalystProxy> {
    private final MultilingualService multilingualService;
    private final QuestionService questionService;
    private final MultilingualTextResolver multilingualTextResolver;
    private final ResolverHelper resolverHelper;
    private final Map<Class<?>, CatalystStrategy<CatalystProxy>> strategies = ImmutableMap.<Class<?>, CatalystStrategy<CatalystProxy>>builder()
            .put(Catalyst.class, new CatalystModelStrategy())
            .put(CatalystDto.class, new CatalystDtoStrategy())
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

    public String getDescription(@NonNull final CatalystProxy proxy, @NonNull final DataFetchingEnvironment environment) {
        return strategies.get(proxy.getObject().getClass()).getDescription(proxy, resolverHelper.getLanguageCode(environment) );
    }

    @NonNull
    public List<LikertQuestion> getQuestions(@NonNull final CatalystProxy proxy) {
        return strategies.get(proxy.getObject().getClass()).getQuestions(proxy);
    }

    @Deprecated
    @Nullable
    public String getCatalystQuestion(@NonNull final CatalystProxy proxy, @NonNull final DataFetchingEnvironment environment) {
        return null;
    }

    public Collection<OpenQuestion> getOpenQuestions(@NonNull final CatalystProxy proxy) {
        return strategies.get(proxy.getObject().getClass()).getOpenQuestions(proxy);
    }


    @NonNull
    public List<DriverProxy> getDrivers(@NonNull final CatalystProxy proxy) {
        return strategies.get(proxy.getObject().getClass()).getDrivers(proxy);
    }

    private interface CatalystStrategy<T extends OutputProxy<?>> {
        @NonNull String getTitle(@NonNull T output, @NonNull String languageTag);
        @Nullable String getDescription(@NonNull T output, @NonNull String languageTag);
        @NonNull List<DriverProxy> getDrivers(@NonNull T output);
        @NonNull List<LikertQuestion> getQuestions(@NonNull T output);
        @Nullable String getCatalystQuestion(@NonNull T output, @NonNull String languageTag);
        @NonNull List<OpenQuestion> getOpenQuestions(@NonNull T output);
    }

    private class CatalystDtoStrategy implements CatalystStrategy<CatalystProxy> {
        @NonNull
        @Override
        public String getTitle(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            final CatalystDto catalyst = (CatalystDto) proxy.getObject();
            return multilingualService.lookupPhrase(catalyst.getTitleId(), languageTag);
        }

        @Nullable
        @Override
        public String getDescription(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            return null;
        }

        @NonNull
        @Override
        public List<DriverProxy> getDrivers(@NonNull final CatalystProxy proxy) {
            final CatalystDto catalyst = (CatalystDto) proxy.getObject();
            return toProxies(questionService.getAllCatalystDrivers(catalyst));
        }

        @NonNull
        @Override
        public List<LikertQuestion> getQuestions(@NonNull final CatalystProxy proxy) {
            return ImmutableList.of();
        }

        @Nullable
        @Override
        public String getCatalystQuestion(@NonNull final CatalystProxy proxy, @NonNull final  String languageTag) {
            return null;
        }

        @NonNull
        @Override
        public List<OpenQuestion> getOpenQuestions(@NonNull final CatalystProxy proxy) {
            return ImmutableList.of();
        }
    }

    private class CatalystModelStrategy implements CatalystStrategy<CatalystProxy> {
        @NonNull
        @Override
        public String getTitle(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            final Catalyst catalyst = (Catalyst) proxy.getObject();
            return multilingualTextResolver.getRequiredText(catalyst.getTitles(), languageTag);
        }

        @Nullable
        @Override
        public String getDescription(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            final Catalyst catalyst = (Catalyst) proxy.getObject();
            return multilingualTextResolver.getOptionalText(catalyst.getDescriptions(), languageTag);
        }

        @NonNull
        @Override
        public List<DriverProxy> getDrivers(@NonNull final CatalystProxy proxy) {
            final Catalyst catalyst = (Catalyst) proxy.getObject();
            return toProxies(catalyst.getDrivers());
        }

        @NonNull
        @Override
        public List<LikertQuestion> getQuestions(@NonNull final CatalystProxy proxy) {
            final Catalyst catalyst = (Catalyst) proxy.getObject();
            return catalyst.getQuestions();
        }

        @Nullable
        @Override
        public String getCatalystQuestion(@NonNull final CatalystProxy proxy, @NonNull final String languageTag) {
            return null;
        }

        @NonNull
        @Override
        public List<OpenQuestion> getOpenQuestions(@NonNull final CatalystProxy proxy) {
            final Catalyst catalyst = (Catalyst) proxy.getObject();
            return catalyst.getOpenQuestions();
        }
    }
}
