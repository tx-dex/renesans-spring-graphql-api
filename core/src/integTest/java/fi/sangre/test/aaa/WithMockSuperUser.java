package fi.sangre.test.aaa;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockSuperUserSecurityContextFactory.class)
public @interface WithMockSuperUser {

    long id() default 1;
    String username() default "admin";
    String password() default "1234";
    String fistName() default "admin";
    String lastName() default "admin";
    String email() default "admin@example.com";

}