package fi.sangre.renesans.aaa;

import com.google.common.collect.ImmutableList;
import fi.sangre.renesans.application.model.SurveyId;
import fi.sangre.renesans.application.model.respondent.RespondentId;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

import static fi.sangre.renesans.aaa.Permissions.ROLE_RESPONDENT;

@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class RespondentPrincipal implements UserDetails {
    private static final List<SimpleGrantedAuthority> RESPONDENT_ROLE = ImmutableList.of(new SimpleGrantedAuthority(ROLE_RESPONDENT));

    private final RespondentId id;
    private final String email;
    private final SurveyId surveyId;

    public RespondentId getId() {
        return id;
    }

    public SurveyId getSurveyId() {
        return surveyId;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return RESPONDENT_ROLE;
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
        return true;
    }
}
