package fi.sangre.renesans.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.sangre.renesans.Application;
import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.repository.UserRepository;
import fi.sangre.renesans.service.MailService;
import fi.sangre.renesans.aaa.WithMockSuperUser;
import fi.sangre.test.graphql.SpringGraphqlExecutor;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.shaded.com.google.common.collect.ImmutableList;

import java.io.File;
import java.time.Duration;
import java.util.Map;

import static fi.sangre.test.graphql.GraphqlServletRequestBuilders.post;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
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
@ContextConfiguration(initializers = {UserIntegTest.Initializer.class})
public class UserIntegTest {
    private static final String RANDOM_DB_NAME = "bsxbnbjqna";
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
    private String graphqlEndpoint;

    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Autowired
    JwtTokenService tokenService;
    @SpyBean
    MailService mailService;

    private SpringGraphqlExecutor executor;

    @BeforeEach
    public void setup() {
        this.executor = new SpringGraphqlExecutor(port, graphqlEndpoint);
    }

    @Test
    @WithMockSuperUser
    public void shouldChangeItsOwnPassword() throws Exception {
        final User user1 = User.builder().username("testuser1").email("test1@mail.com").password(passwordEncoder.encode("1234")).enabled(true).build();
        userRepository.save(user1);

        final String oldPassLoginQuery = "mutation {\n" +
                "  login(username: \"testuser1\", password: \"1234\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";
        final String loginResult = executor.perform(post()
                .content(oldPassLoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        final String loginToken = new ObjectMapper().readTree(loginResult).get("data").get("login").get("token").textValue();

        final String query = "mutation {\n" +
                "  updatePassword (\n" +
                "    id: " + user1.getId() +
                "    oldPassword: \"1234\"\n" +
                "    newPassword: \"12345\"\n" +
                "  )\n" +
                "}";

        executor.perform(post()
                .content(query)
                .header("Authorization", "Bearer " + loginToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatePassword", is(true)))
                .andExpect(jsonPath("$.errors").doesNotExist());

        executor.perform(post()
                .content(oldPassLoginQuery))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message", is("Bad credentials")))
                .andExpect(jsonPath("$.errors.[0].errorType", is("DataFetchingException")));

        final String newPassLoginQuery = "mutation {\n" +
                "  login(username: \"testuser1\", password: \"12345\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";

        executor.perform(post()
                .content(newPassLoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());

    }

    @Test
    @WithMockSuperUser
    public void shouldNotUpdateOtherUsersPassword() throws Exception {
        final User user2 = User.builder().username("testuser2").email("test2@mail.com").password(passwordEncoder.encode("123456")).enabled(true).build();
        final User user3 = User.builder().username("testuser3").email("test3@mail.com").password(passwordEncoder.encode("654321")).enabled(true).build();
        userRepository.saveAll(ImmutableList.of(user2, user3));

        final String user2LoginQuery = "mutation {\n" +
                "  login(username: \"testuser2\", password: \"123456\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";
        final String loginResult = executor.perform(post()
                .content(user2LoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        final String loginToken = new ObjectMapper().readTree(loginResult).get("data").get("login").get("token").textValue();

        final String query = "mutation {\n" +
                "  updatePassword (\n" +
                "    id: " + user3.getId() +
                "    oldPassword: \"654321\"\n" +
                "    newPassword: \"1234\"\n" +
                "  )\n" +
                "}";

        executor.perform(post()
                .content(query)
                .header("Authorization", "Bearer " + loginToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatePassword", is(nullValue())))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message", is("Access is denied")))
                .andExpect(jsonPath("$.errors.[0].errorType", is("DataFetchingException")));

        final String user3LoginQuery = "mutation {\n" +
                "  login(username: \"testuser3\", password: \"654321\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";

        executor.perform(post()
                .content(user3LoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());
    }

    @Test
    @WithMockSuperUser
    public void shouldNotUpdatePasswordWhenOldPasswordIsWrong() throws Exception {
        final User user4 = User.builder().username("testuser4").email("test4@mail.com").password(passwordEncoder.encode("1234")).enabled(true).build();
        userRepository.save(user4);

        final String user4LoginQuery = "mutation {\n" +
                "  login(username: \"testuser4\", password: \"1234\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";
        final String loginResult = executor.perform(post()
                .content(user4LoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist())
                .andReturn().getResponse().getContentAsString();
        final String loginToken = new ObjectMapper().readTree(loginResult).get("data").get("login").get("token").textValue();

        final String query = "mutation {\n" +
                "  updatePassword (\n" +
                "    id: " + user4.getId() +
                "    oldPassword: \"xxxx\"\n" +
                "    newPassword: \"123456\"\n" +
                "  )\n" +
                "}";

        executor.perform(post()
                .content(query)
                .header("Authorization", "Bearer " + loginToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatePassword", is(nullValue())))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message", startsWith("Cannot update password")))
                .andExpect(jsonPath("$.errors.[0].errorType", is("DataFetchingException")));

        final String user3LoginQuery = "mutation {\n" +
                "  login(username: \"testuser4\", password: \"1234\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";

        executor.perform(post()
                .content(user3LoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());

    }

    @Test
    @WithMockSuperUser
    public void shouldNotUpdatePasswordWhenNotAuthenticated() throws Exception {
        final User user5 = User.builder().username("testuser5").email("test5@mail.com").password(passwordEncoder.encode("12345")).enabled(true).build();
        userRepository.save(user5);

        final String query = "mutation {\n" +
                "  updatePassword (\n" +
                "    id: " + user5.getId() +
                "    oldPassword: \"12345\"\n" +
                "    newPassword: \"1234\"\n" +
                "  )\n" +
                "}";

        executor.perform(post()
                .content(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatePassword", is(nullValue())))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors.[0].message", is("Access is denied")))
                .andExpect(jsonPath("$.errors.[0].errorType", is("DataFetchingException")));

    }

    @Test
    @WithMockSuperUser
    public void shouldSendResetPasswordLink() throws Exception {
        final User user6 = User.builder().username("testuser6").email("test6@mail.com").password(passwordEncoder.encode("12")).enabled(true).build();
        userRepository.save(user6);

        final String oldPassLoginQuery = "mutation {\n" +
                "  login(username: \"testuser6\", password: \"12\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";
        executor.perform(post()
                .content(oldPassLoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());

        final String query = "mutation {\n" +
                "  requestPasswordReset (\n" +
                "    email: \"test6@mail.com\"\n" +
                "  )\n" +
                "}";

        doNothing().when(mailService).sendEmail(any(), any(), any(), any(), any());

        executor.perform(post()
                .content(query))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.requestPasswordReset", is(true)))
                .andExpect(jsonPath("$.errors").doesNotExist());

        ArgumentCaptor<Map> replacements = ArgumentCaptor.forClass(Map.class);
        verify(mailService, times(1)).sendEmail(eq("wecan-reset-user-password"), any(), any(), eq("test6@mail.com"), replacements.capture());

        final String resetToken = ((String) replacements.getValue().get("reset_link")).replaceFirst(".*reset/", "");

        final String resetPasswordQuery = "mutation {\n" +
                "  resetUserPassword (\n" +
                "    token: \"" + resetToken + "\"\n" +
                "    newPassword: \"1234\"\n" +
                "  )\n" +
                "}";

        executor.perform(post()
                .content(resetPasswordQuery))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.resetUserPassword", is(true)))
                .andExpect(jsonPath("$.errors").doesNotExist());

        final String newPassLoginQuery = "mutation {\n" +
                "  login(username: \"testuser6\", password: \"1234\") {\n" +
                "    token\n" +
                "  }\n" +
                "}";
        executor.perform(post()
                .content(newPassLoginQuery))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.login.token").exists())
                .andExpect(jsonPath("$.errors").doesNotExist());

    }

}
