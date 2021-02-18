package fi.sangre.test.docker;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;

public class DataDbInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    private static String RANDOM_DB_NAME;

    private static DockerComposeContainer<?> dev;

    public static DockerComposeContainer<?> bootstrap(final DockerComposeContainer<?> dev) {
        DataDbInitializer.dev = dev;

        DataDbInitializer.RANDOM_DB_NAME = RandomStringUtils.randomAlphabetic(8);

        DataDbInitializer.dev.withExposedService("data-db", 1,  5432, Wait
                .forLogMessage(".*database system is ready to accept connections.*", 2)
                .withStartupTimeout(Duration.ofSeconds(30)))
                .withEnv("DB_NAME", RANDOM_DB_NAME);

        return dev;
    }


    @Override
    public void initialize(final ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                "spring.flyway.url=" + String.format("jdbc:postgresql://%s:%s/" + RANDOM_DB_NAME,
                        dev.getServiceHost("data-db", 5432), dev.getServicePort("data-db", 5432)), //overriding with mapped host
                "spring.datasource.url=" + String.format("jdbc:postgresql://%s:%s/" + RANDOM_DB_NAME,
                        dev.getServiceHost("data-db", 5432), dev.getServicePort("data-db", 5432))) //overriding with mapped host
                .applyTo(configurableApplicationContext.getEnvironment());
    }
}
