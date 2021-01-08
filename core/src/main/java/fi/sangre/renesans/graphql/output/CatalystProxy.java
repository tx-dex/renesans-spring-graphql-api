package fi.sangre.renesans.graphql.output;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Data
@ToString
public class CatalystProxy implements OutputProxy<CatalystOutput> {
    private final CatalystOutput object;

    public UUID getId() {
        return object.getId().getValue();
    }

    public static <T extends CatalystOutput> CatalystProxy toProxy(@NonNull final T catalyst) {
        return new CatalystProxy(catalyst);
    }

    public static  <T extends CatalystOutput> List<CatalystProxy> toProxies(@NonNull final List<T> catalysts) {
        return catalysts.stream()
                .map(CatalystProxy::new)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
