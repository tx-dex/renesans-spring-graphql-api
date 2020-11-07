package fi.sangre.renesans.graphql;

import fi.sangre.renesans.Application;
import fi.sangre.test.graphql.GraphqlServletRequestBuilders;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.DockerComposeContainer;
import fi.sangre.test.graphql.SpringGraphqlExecutor;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;
import java.time.Duration;

import static fi.sangre.test.graphql.GraphqlServletRequestBuilders.post;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@TestPropertySource(properties = {
        "RENESANS_ETC=etc",
        "ETC_PATH=classpath",
        "FLYWAY_DB_USER=masteradmin",
        "FLYWAY_DB_PASSWORD=qazwsx",
        "DB_USER=masteradmin",
        "DB_PASSWORD=qazwsx",
        "HIBERNATE_USE_2ND_LEVEL_CACHE=false",
        "HIBERNATE_USE_QUERY_CACHE=false",
})
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {CustomerServiceIntegTest.Initializer.class})
public class CustomerServiceIntegTest {
    private static final String RANDOM_DB_NAME = "rwxvdshiij";
    @ClassRule
    public static DockerComposeContainer<?> dev = new DockerComposeContainer<>(new File("src/integTest/resources/compose.yml"))
            .withExposedService("data-db", 1,  5432, Wait
                    .forLogMessage(".*database system is ready to accept connections.*", 2)
                    .withStartupTimeout(Duration.ofSeconds(30)))
            .withEnv("DB_NAME", RANDOM_DB_NAME);

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
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

    @BeforeAll
    public static void setupServices() {
        dev.start();
    }

    @LocalServerPort
    private Long port;
    @Value("${graphql.servlet.mapping}")
    private String endpoint;

    private SpringGraphqlExecutor executor;

    @BeforeEach
    public void setup() {
        this.executor = new SpringGraphqlExecutor(port, endpoint);
    }

    @Test
    public void shouldGetAccessIsDeniedError() throws Exception {

        final String query = "query {" +
                "  customers(languageCode: \"en\") {" +
                "    id," +
                "    description" +
                "  }" +
                "}";

        executor.perform(post()
                .content(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customers", is(nullValue())))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message", is("Access is denied")))
                .andExpect(jsonPath("$.errors.[0].errorType", is("DataFetchingException")));
    }

    @Test
    public void shouldGetAllCustomersForSuperUser() throws Exception {

        final String query = "query {" +
                "  customers(languageCode: \"en\") {" +
                "    id," +
                "    description" +
                "  }" +
                "}";

        executor.perform(post()
                .content(query)
                .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIxIiwicm9sZXMiOlsiUk9MRV9TVVBFUl9VU0VSIl0sImV4cCI6MTUzMjAwNTA3NywiaWF0IjoxNTMxNDAwMjc3fQ.oylTX-UQiuQZ-Uy8CrRUNhD-qPnh09vqNObhxVcS6OQza789dSwX7RSkXvlh4x2Hr0umUGuyH-J5pERb7_9D3Q"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customers", hasSize(3)))
                .andExpect(jsonPath("$.data.customers.[0].id", is("1")))
                .andExpect(jsonPath("$.data.customers.[1].id", is("2")))
                .andExpect(jsonPath("$.data.customers.[2].id", is("3")))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    public void shouldGetFilteredCustomersForPowerUser() throws Exception {

        final String query = "query {" +
                "  customers(languageCode: \"en\") {" +
                "    id," +
                "    description" +
                "  }" +
                "}";

        executor.perform(post()
                .content(query)
                .header("Authorization", "Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIyIiwicm9sZXMiOlsiUk9MRV9QT1dFUl9VU0VSIl0sImV4cCI6MTUzMjAwNjYxMSwiaWF0IjoxNTMxNDAxODExfQ._aE9PZGEulLw7-HRv_TpHszYZECLG2IJS85Wv968cIF6jGoUr5dEjYHACa3LByrlNeBHgQyGv3KmnYHy8S8e6Q"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.customers", hasSize(2)))
                .andExpect(jsonPath("$.data.customers.[0].id", is("2")))
                .andExpect(jsonPath("$.data.customers.[1].id", is("3")))
                .andExpect(jsonPath("$.errors").doesNotExist());
    }
}