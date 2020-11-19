package fi.sangre.renesans.graphql.output;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.lang.NonNull;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
@Data
@ToString
public class DriverProxy implements OutputProxy<DriverOutput> {
    private final DriverOutput object;

    public Long getId() {
        return object.getId();
    }

    public static <T extends DriverOutput> DriverProxy toProxy(@NonNull final T driver) {
        return new DriverProxy(driver);
    }

    public static <T extends DriverOutput> List<DriverProxy> toProxies(@NonNull final List<T> drivers) {
        return drivers.stream()
                .map(DriverProxy::toProxy)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }
}
