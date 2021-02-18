package fi.sangre.renesans.aaa;

import fi.sangre.renesans.Application;
import fi.sangre.test.docker.DataDbInitializer;
import org.junit.ClassRule;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
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
@ContextConfiguration(initializers = {DataDbInitializer.class})
class UserPrincipalServiceIntegTest {
    @ClassRule
    public static DockerComposeContainer<?> dev = DataDbInitializer.bootstrap(
            new DockerComposeContainer<>(new File("src/integTest/resources/compose.yml")));

    @BeforeAll
    public static void setupServices() {
        dev.start();
    }

    @Autowired
    private UserPrincipalService userPrincipalService;

    @Order(1)
    @Test
    @Sql("insert-admin.sql")
    public void shouldLoadUserByUsername() throws Exception {
        final UserDetails admin =  assertDoesNotThrow(() -> userPrincipalService.loadUserByUsername("admin"));


    }

    @Order(2)
    @Test
    public void shouldLoadUserById() throws Exception {
        final UserDetails admin =  assertDoesNotThrow(() -> userPrincipalService.loadUserById(1L));

    }

}