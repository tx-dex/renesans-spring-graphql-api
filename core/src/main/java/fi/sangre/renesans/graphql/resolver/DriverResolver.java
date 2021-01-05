package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import com.google.common.collect.ImmutableMap;
import fi.sangre.renesans.application.model.Driver;
import fi.sangre.renesans.dto.DriverDto;
import fi.sangre.renesans.graphql.output.DriverProxy;
import fi.sangre.renesans.graphql.output.OutputProxy;
import fi.sangre.renesans.service.MultilingualService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Map;

@RequiredArgsConstructor

@Component
public class DriverResolver implements GraphQLResolver<DriverProxy> {
    private final MultilingualService multilingualService;
    private final ResolverHelper resolverHelper;
    private final MultilingualTextResolver multilingualTextResolver;
    private final Map<Class<?>, DriverStrategy<DriverProxy>> strategies = ImmutableMap.<Class<?>, DriverStrategy<DriverProxy>>builder()
            .put(Driver.class, new DriverModelStrategy())
            .put(DriverDto.class, new DriverDtoStrategy())
            .build();

    @NonNull
    public Long getId(@NonNull final DriverProxy proxy) {
        return proxy.getId();
    }

    @NonNull
    public String getTitle(@NonNull final DriverProxy proxy, @NonNull final DataFetchingEnvironment environment) {
        return strategies.get(proxy.getObject().getClass()).getTitle(proxy, resolverHelper.getLanguageCode(environment));
    }

    @Deprecated
    public Long getTitleId(@NonNull final DriverProxy proxy) {
        return null;
    }

    @Nullable
    public String getDescription(@NonNull final DriverProxy proxy, final DataFetchingEnvironment environment) {
        return strategies.get(proxy.getObject().getClass()).getDescription(proxy, resolverHelper.getLanguageCode(environment));
    }

    @Nullable
    public String getPrescription(@NonNull final DriverProxy proxy, final DataFetchingEnvironment environment) {
        return strategies.get(proxy.getObject().getClass()).getPrescription(proxy, resolverHelper.getLanguageCode(environment));
    }

    @NonNull
    public Double getWeight(@NonNull final DriverProxy proxy) {
        return proxy.getObject().getWeight();
    }

    private interface DriverStrategy<T extends OutputProxy<?>> {
        @NonNull  String getTitle(@NonNull T proxy, @NonNull String languageTag);
        @Nullable String getDescription(@NonNull T proxy, @NonNull String languageTag);
        @Nullable String getPrescription(@NonNull T proxy, @NonNull String languageTag);
    }

    private class DriverDtoStrategy implements DriverStrategy<DriverProxy> {
        @NonNull
        @Override
        public String getTitle(@NonNull final DriverProxy proxy, @NonNull final String languageTag) {
            final DriverDto driver = (DriverDto) proxy.getObject();
            return multilingualService.lookupPhrase(driver.getTitleId(), languageTag);
        }

        @Nullable
        @Override
        public String getDescription(@NonNull final DriverProxy proxy, @NonNull final String languageTag) {
            final DriverDto driver = (DriverDto) proxy.getObject();
            return multilingualService.lookupPhrase(driver.getDescriptionId(), languageTag);
        }

        @Nullable
        @Override
        public String getPrescription(@NonNull final DriverProxy proxy, @NonNull final String languageTag) {
            final DriverDto driver = (DriverDto) proxy.getObject();
            return multilingualService.lookupPhrase(driver.getPrescriptionId(), languageTag);
        }
    }

    private class DriverModelStrategy implements DriverStrategy<DriverProxy> {
        @NonNull
        @Override
        public String getTitle(@NonNull final DriverProxy proxy, @NonNull final String languageTag) {
            final Driver driver = (Driver) proxy.getObject();
            return multilingualTextResolver.getRequiredText(driver.getTitles(), languageTag);
        }

        @Nullable
        @Override
        public String getDescription(@NonNull final DriverProxy proxy, @NonNull final String languageTag) {
            final Driver driver = (Driver) proxy.getObject();
            return multilingualTextResolver.getOptionalText(driver.getDescriptions(), languageTag);
        }

        @Nullable
        @Override
        public String getPrescription(@NonNull final DriverProxy proxy, @NonNull final String languageTag) {
            final Driver driver = (Driver) proxy.getObject();
            return multilingualTextResolver.getOptionalText(driver.getPrescriptions(), languageTag);
        }
    }
}
