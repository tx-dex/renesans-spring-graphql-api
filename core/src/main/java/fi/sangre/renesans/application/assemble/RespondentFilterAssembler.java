package fi.sangre.renesans.application.assemble;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.application.model.filter.RespondentFilter;
import fi.sangre.renesans.application.model.filter.RespondentParameterFilter;
import fi.sangre.renesans.application.utils.UUIDUtils;
import fi.sangre.renesans.graphql.input.FilterInput;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Slf4j

@Component
public class RespondentFilterAssembler {
    private static final Set<String> STANDARD_FILTERS = ImmutableSet.of();

    private final UUIDUtils uuidUtils;

    @NonNull
    public List<RespondentFilter> fromInput(@Nullable final List<FilterInput> inputs) {
        final ImmutableList.Builder<RespondentFilter> filters = ImmutableList.builder();

        if (inputs != null) {
            for(final FilterInput input : inputs) {
                if (input.getValues() != null && !input.getValues().isEmpty()) {
                    final String id = input.getId();
                    if (STANDARD_FILTERS.contains(id)) {
                        log.debug("No standard filters yet");
                    } else {
                        filters.add(RespondentParameterFilter.builder()
                                .id(uuidUtils.from(id))
                                .values(input.getValues().stream()
                                        .map(uuidUtils::from)
                                        .collect(collectingAndThen(toList(), Collections::unmodifiableList)))
                                .build());
                    }
                }
            }
        } else {
            log.trace("Empty filters");
        }

        return filters.build();
    }
}
