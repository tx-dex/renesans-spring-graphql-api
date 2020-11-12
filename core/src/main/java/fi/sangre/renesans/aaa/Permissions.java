package fi.sangre.renesans.aaa;

import fi.sangre.renesans.model.Respondent;
import fi.sangre.renesans.model.RespondentGroup;
import fi.sangre.renesans.persistence.model.Customer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;

@Component
public class Permissions implements PermissionEvaluator {
    public static final String SUPER_USER = "ROLE_SUPER_USER";
    public static final String POWER_USER = "ROLE_POWER_USER";

    private final UserPrincipalService userPrincipalService;

    @Autowired
    public Permissions(final UserPrincipalService userPrincipalService) {
        checkArgument(userPrincipalService != null, "UserPrincipalService is required");

        this.userPrincipalService = userPrincipalService;
    }

    static <T extends GrantedAuthority> boolean hasRole(final Collection<T> authorities, final String role) {
        for (final GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    private UserPrincipal principal(Authentication authentication) {
        return (UserPrincipal) authentication.getPrincipal();
    }

    private boolean permit(UserPrincipal powerUser, Customer customer, Authentication authentication, Object permission) {
            final Set<Long> customerIds = userPrincipalService.getCustomerIdsThatPrincipalCanAccess(powerUser);

            return customerIds.contains(customer.getId());
    }

    private boolean permit(UserPrincipal powerUser, RespondentGroup respondentGroup, Authentication authentication, Object permission) {
        final Set<Long> customerIds = userPrincipalService.getCustomerIdsThatPrincipalCanAccess(powerUser);

        return customerIds.contains(respondentGroup.getCustomerId());
    }

    private boolean permit(UserPrincipal powerUser, Respondent respondent, Authentication authentication, Object permission) {
        final Set<String> respondentGroupIds = userPrincipalService.getRespondentGroupIdsThatPrincipalCanAccess(powerUser);

        return respondentGroupIds.contains(respondent.getRespondentGroupId());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (hasRole(authentication.getAuthorities(), SUPER_USER)) {
            return true;
        }

        if (hasRole(authentication.getAuthorities(), POWER_USER)) {
            if (targetDomainObject instanceof Customer) {
                return permit(principal(authentication), (Customer) targetDomainObject, authentication, permission);
            } else if (targetDomainObject instanceof RespondentGroup) {
                return permit(principal(authentication), (RespondentGroup) targetDomainObject, authentication, permission);
            } else if (targetDomainObject instanceof Respondent) {
                return permit(principal(authentication), (Respondent) targetDomainObject, authentication, permission);
            } else if (targetDomainObject instanceof Optional) {
                final Optional optionalDomainObject = (Optional) targetDomainObject;
                if (optionalDomainObject.isPresent()) {
                    if (optionalDomainObject.get() instanceof Customer) {
                        return permit(principal(authentication), (Customer) optionalDomainObject.get(), authentication, permission);
                    } else if (optionalDomainObject.get() instanceof RespondentGroup) {
                        return permit(principal(authentication), (RespondentGroup) optionalDomainObject.get(), authentication, permission);
                    } else if (optionalDomainObject.get() instanceof Respondent) {
                        return permit(principal(authentication), (Respondent) optionalDomainObject.get(), authentication, permission);
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
        return false;
    }
}
