package fi.sangre.renesans.aaa;

import fi.sangre.renesans.exception.UserNotFoundException;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.Set;
import java.util.stream.Collectors;

import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;
import static fi.sangre.renesans.aaa.CacheConfig.AUTH_RESPONDENT_GROUP_IDS_CACHE;

@RequiredArgsConstructor
@Slf4j

@Service
@CacheConfig(cacheManager = "authorizationCacheManager")
public class UserPrincipalService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    // This method is used in the built in Spring Authentication provider
    public UserDetails loadUserByUsername(final String username)
            throws UsernameNotFoundException {

        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username or : " + username));

        return UserPrincipal.create(user);
    }

    // This method is used by JWTAuthenticationFilter
    @NotNull
    @Transactional
    public UserDetails loadUserById(final Long id) {

        final User user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id : " + id));

        if (!user.isEnabled()) {
            log.warn("User {} is disabled", user.getUsername());
            throw new DisabledException("User is disabled");
        }

        return UserPrincipal.create(user);
    }

    @Cacheable(cacheNames = AUTH_CUSTOMER_IDS_CACHE, key = "#user.id")
    public Set<Long> getCustomerIdsThatPrincipalCanAccess(final UserPrincipal user) {
        log.info("Get customer ids for principal: {}", user);
        Set<Long> customersIds = userRepository.findCustomerIdsAccessibleByUserId(user.getId()).stream().map(BigInteger::longValue).collect(Collectors.toSet());
        log.debug("Found {} customer ids for principal: {}", customersIds.size(), user.getUsername());
        return customersIds;
    }

    @Cacheable(cacheNames = AUTH_RESPONDENT_GROUP_IDS_CACHE, key = "#user.id")
    public Set<String> getRespondentGroupIdsThatPrincipalCanAccess(final UserPrincipal user) {
        log.info("Get respondent group ids for principal: {}", user);
        Set<String> respondentGroupIds = userRepository.findRespondentGroupIdsAccessibleByUserId(user.getId());
        log.debug("Found {} respondent group ids for principal: {}", respondentGroupIds.size(), user.getUsername());
        return respondentGroupIds;
    }

    public UserPrincipal getLoggedInPrincipal() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }

        return (UserPrincipal) authentication.getPrincipal();
    }

    public User getLoggedInUser() { //This maybe use as a implementation of AuditorAware<User>
        final UserPrincipal user = getLoggedInPrincipal();
        if (user != null) {
            final Long userId = user.getId();

            return userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(userId));
        } else {
            return null;
        }
    }

    public boolean isSuperUser(final UserPrincipal user) {
        return Permissions.hasRole(user.getAuthorities(), Permissions.SUPER_USER);
    }
}
