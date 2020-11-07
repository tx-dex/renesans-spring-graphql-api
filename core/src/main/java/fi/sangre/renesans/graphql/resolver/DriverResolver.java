package fi.sangre.renesans.graphql.resolver;

import com.coxautodev.graphql.tools.GraphQLResolver;
import fi.sangre.renesans.dto.DriverDto;
import fi.sangre.renesans.service.MultilingualService;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor

@Component
public class DriverResolver implements GraphQLResolver<DriverDto> {
    private final MultilingualService multilingualService;
    private final ResolverHelper helper;

    public String getTitle(final DriverDto driver, final DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(driver.getTitleId(), helper.getLanguageCode(environment));
    }

    public String getDescription(final DriverDto driver, final DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(driver.getDescriptionId(), helper.getLanguageCode(environment));
    }

    // do not expose internals to the public
    @PreAuthorize("hasRole('SUPER_USER')")
    public String getPrescription(final DriverDto driver, final DataFetchingEnvironment environment) {
        return multilingualService.lookupPhrase(driver.getPrescriptionId(), helper.getLanguageCode(environment));
    }
}
