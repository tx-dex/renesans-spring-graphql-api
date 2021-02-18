package fi.sangre.renesans.config;


import fi.sangre.renesans.persistence.auditing.ApiContextHelper;
import fi.sangre.renesans.persistence.auditing.SecurityAuditorAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.lang.NonNull;

@EnableJpaAuditing(auditorAwareRef = "security-auditor")
@Configuration
public class PersistenceConfig {

    @Bean(name = "security-auditor")
    public SecurityAuditorAware securityAuditorAware(@NonNull final ApiContextHelper apiContextHelper) {
        return new SecurityAuditorAware(apiContextHelper);
    }
}
