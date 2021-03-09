package fi.sangre.renesans.aaa;

import com.google.common.collect.Sets;
import fi.sangre.renesans.application.model.Organization;
import fi.sangre.renesans.persistence.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j

@Component
public class Permissions implements PermissionEvaluator {
    public static final String SUPER_USER = "ROLE_SUPER_USER";
    public static final String POWER_USER = "ROLE_POWER_USER";
    public static final String ROLE_RESPONDENT = "ROLE_RESPONDENT";
    public static final String ROLE_GUEST = "ROLE_GUEST";

    public static final String ORGANIZATION_TARGET = "organization";
    public static final String SURVEY_TARGET = "survey";

    private final UserPrincipalService userPrincipalService;
    private final SurveyMappingService surveyMappingService;

    static <T extends GrantedAuthority> boolean hasRole(final Collection<T> authorities, final String role) {
        for (final GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private UserPrincipal user(final @NonNull Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }

    @NonNull
    private RespondentPrincipal respondent(final @NonNull Authentication authentication) {
        return (RespondentPrincipal) authentication.getPrincipal();
    }

    @NonNull
    private GuestPrincipal guest(final @NonNull Authentication authentication) {
        return (GuestPrincipal) authentication.getPrincipal();
    }


    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (hasRole(authentication.getAuthorities(), SUPER_USER)) {
            return true;
        } else if (hasRole(authentication.getAuthorities(), POWER_USER)) {
            if (targetDomainObject instanceof Organization) {
                return permitOrganization(user(authentication), ((Organization) targetDomainObject).getId(), authentication, permission);
            } else if (targetDomainObject instanceof Customer) {
                return permitOrganization(user(authentication), ((Customer) targetDomainObject).getId(), authentication, permission);
            } else if (targetDomainObject instanceof Optional) {
                final Optional<?> optionalDomainObject = (Optional<?>) targetDomainObject;
                if (optionalDomainObject.isPresent()) {
                    if (optionalDomainObject.get() instanceof Organization) {
                        return permitOrganization(user(authentication), ((Organization) optionalDomainObject.get()).getId(), authentication, permission);
                    } else if (optionalDomainObject.get() instanceof Customer) {
                        return permitOrganization(user(authentication), ((Customer) optionalDomainObject.get()).getId(), authentication, permission);
                    }
                } else {
                    return true; //Allow access when there is null found
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (hasRole(authentication.getAuthorities(), SUPER_USER)) {
            return true;
        } else if (hasRole(authentication.getAuthorities(), POWER_USER)) {
            if (ORGANIZATION_TARGET.equalsIgnoreCase(targetType)) {
                return permitOrganization(user(authentication), (UUID) targetId, authentication, permission);
            } else if (SURVEY_TARGET.equalsIgnoreCase(targetType)) {
                return permitSurvey(user(authentication), (UUID) targetId, authentication, permission);
            }
        } else if (hasRole(authentication.getAuthorities(), ROLE_RESPONDENT)) {
            if (SURVEY_TARGET.equalsIgnoreCase(targetType)) {
                return permitSurvey(respondent(authentication), (UUID) targetId, authentication, permission);
            }
        } else if (hasRole(authentication.getAuthorities(), ROLE_GUEST)) {
            if (SURVEY_TARGET.equalsIgnoreCase(targetType)) {
                return permitSurvey(guest(authentication), (UUID) targetId, authentication, permission);
            }
        }

        return false;
    }

    private boolean permitOrganization(@NonNull final UserPrincipal powerUser,
                                       @NonNull final UUID organizationId,
                                       @NonNull final Authentication authentication,
                                       @NonNull final Object permission) {
        final Set<UUID> organizationIds = userPrincipalService.getCustomerIdsThatPrincipalCanAccess(powerUser);

        return organizationIds.contains(organizationId);
    }

    private boolean permitSurvey(@NonNull final RespondentPrincipal respondent,
                                 @NonNull final UUID questionnaireId,
                                 @NonNull final Authentication authentication,
                                 @NonNull final Object permission) {
        return respondent.getId().getValue().equals(questionnaireId);
    }

    private boolean permitSurvey(@NonNull final GuestPrincipal guest,
                                 @NonNull final UUID questionnaireId,
                                 @NonNull final Authentication authentication,
                                 @NonNull final Object permission) {
        return guest.getId().getValue().equals(questionnaireId);
    }

    private boolean permitSurvey(@NonNull final UserPrincipal user,
                                 @NonNull final UUID questionnaireId,
                                 @NonNull final Authentication authentication,
                                 @NonNull final Object permission) {
        final Set<UUID> userOrganizationIds = userPrincipalService.getCustomerIdsThatPrincipalCanAccess(user);
        final Set<UUID> surveyOrganizationIds = surveyMappingService.getSurveyOrganizations(questionnaireId);

        return !Sets.intersection(userOrganizationIds, surveyOrganizationIds).isEmpty();
    }
}
