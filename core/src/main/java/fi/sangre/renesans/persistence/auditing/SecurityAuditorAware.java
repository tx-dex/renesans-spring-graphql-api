package fi.sangre.renesans.persistence.auditing;

import fi.sangre.renesans.aaa.UserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
public class SecurityAuditorAware implements AuditorAware<Long> {
    private final ApiContextHelper apiContextHelper;

    @NonNull
    @Override
    public Optional<Long> getCurrentAuditor() {
        return Optional.ofNullable(apiContextHelper.getCurrentUser())
                .map(UserPrincipal::getId);
    }
}
