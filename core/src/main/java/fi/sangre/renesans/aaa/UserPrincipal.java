package fi.sangre.renesans.aaa;

import fi.sangre.renesans.model.User;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@ToString(doNotUseGetters = true, exclude = "password")
@EqualsAndHashCode(of = "id")
public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final boolean isEnabled;
    private final Collection<? extends GrantedAuthority> authorities;

    public static UserPrincipal create(final User user) {
        final List<GrantedAuthority> authorities = user.getRoles().stream().map(role ->
                new SimpleGrantedAuthority(role.getName())
        ).collect(collectingAndThen(toList(), Collections::unmodifiableList));

        return new UserPrincipal(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                authorities
        );
    }

    public Long getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
