package fi.sangre.test.aaa;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.aaa.UserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockSuperUserSecurityContextFactory implements WithSecurityContextFactory<WithMockSuperUser> {
    @Override
    public SecurityContext createSecurityContext(WithMockSuperUser superUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        UserPrincipal principal = new UserPrincipal(superUser.id(),
                superUser.username(),
                superUser.fistName() + " " + superUser.lastName(),
                superUser.fistName(),
                superUser.lastName(),
                superUser.email(),
                new BCryptPasswordEncoder().encode(superUser.password()),
                true,
                ImmutableList.of(new SimpleGrantedAuthority("ROLE_SUPER_USER")));

        Authentication auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }
}