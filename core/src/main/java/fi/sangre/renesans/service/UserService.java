package fi.sangre.renesans.service;

import fi.sangre.renesans.aaa.JwtTokenService;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.application.utils.TokenUtils;
import fi.sangre.renesans.exception.CurrentUserDeleteException;
import fi.sangre.renesans.exception.UserNotFoundException;
import fi.sangre.renesans.model.Role;
import fi.sangre.renesans.model.User;
import fi.sangre.renesans.persistence.model.Customer;
import fi.sangre.renesans.persistence.repository.CustomerRepository;
import fi.sangre.renesans.repository.UserRepository;
import graphql.GraphQLException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static fi.sangre.renesans.aaa.CacheConfig.AUTH_CUSTOMER_IDS_CACHE;
import static fi.sangre.renesans.aaa.CacheConfig.AUTH_RESPONDENT_GROUP_IDS_CACHE;

@RequiredArgsConstructor
@Slf4j

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final TokenService tokenService;
    private final MailService mailService;
    private final RoleService roleService;
    private final CustomerRepository customerRepository;

    public User storeUser(Long id, String firstName, String lastName, String email, String password, String username, Boolean enabled, List<String> roleNames, final String locale) {
        User user;
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        final boolean isCurrentUser = principal.getId().equals(id);
        final boolean sendActivationEmail;

        if (id != null) {
            sendActivationEmail = false;
            user = userRepository.findById(id).orElseThrow(() -> new GraphQLException("Invalid user id"));
            // if changing email check for conflicts
            if (email != null && !user.getEmail().equals(email) && isEmailRegistered(email, id)) {
                throw new GraphQLException("Email address is already registered.");
            }
            // if changing username check for conflicts
            if (username != null && !user.getUsername().equals(username) && isUsernameRegistered(username, id)) {
                throw new GraphQLException("Username is already registered.");
            }
        } else {
            sendActivationEmail = true;
            user = new User();
            if (isEmailRegistered(email)) {
                throw new GraphQLException("Email address is already registered.");
            }
            if (isUsernameRegistered(username)) {
                throw new GraphQLException("Username is already registered.");
            }
        }

        if (firstName != null) {
            user.setFirstName(firstName);
        }

        if (lastName != null) {
            user.setLastName(lastName);
        }

        if (email != null) {
            user.setEmail(email);
        }

        if (username != null) {
            user.setUsername(username);
        }

        if (enabled != null) {
            if (isCurrentUser && !enabled) {
                throw new GraphQLException("Deactivating own account is not allowed.");
            }

            user.setEnabled(enabled);
        }

        if (roleNames != null) {
            if (isCurrentUser) {
                throw new GraphQLException("Editing own user roles is not allowed.");
            }

            List<Role> roles = roleNames.isEmpty()
                    ? new ArrayList<>()
                    : roleService.findByNames(roleNames);

            user.setRoles(roles);
        }

        user = userRepository.save(user);

        if (sendActivationEmail) {
            mailService.sendActivationEmail(user, locale);
        }

        return user;
    }

    @CacheEvict(cacheNames = {AUTH_CUSTOMER_IDS_CACHE, AUTH_RESPONDENT_GROUP_IDS_CACHE}, key = "#id")
    public User setUserAccess(Long id, Long customerId, Boolean allow) {
        User user = userRepository.findById(id).orElseThrow(() -> new GraphQLException("Invalid user id"));
        Customer customer = customerRepository.findById(customerId).orElseThrow(() -> new GraphQLException("Invalid customer id"));

        if (user.getId().equals(customer.getCreatedBy())) {
            throw new GraphQLException("Can't alter user's customer access rights for customers created by the user");
        }

        Set<User> customerUsers = userRepository.findByCustomersContaining(customer);
        Set<Customer> userCustomers = customerRepository.findByUsersContaining(user);

        if (allow) {
            customerUsers.add(user);
            userCustomers.add(customer);
        } else {
            customerUsers.remove(user);
            userCustomers.remove(customer);
        }

        customer.setUsers(customerUsers);
        customerRepository.save(customer);

        user.setCustomers(userCustomers);
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(final Long userId, final String oldPassword, final String newPassword) {

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.debug("Cannot update password, old password doesn't match for the '{}' user", user.getUsername());
            throw new GraphQLException("Cannot update password, old password doesn't match");
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    // used for admin updating users password without knowing the old password
    @Transactional
    @PreAuthorize("hasRole('SUPER_USER')")
    public void updatePassword(final Long userId, final String newPassword) {

        final User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    public void requestPasswordReset(final String email, final String locale) {
        //TODO: validate email

        final Optional<User> user = userRepository.findByEmail(email);

        if (user.isPresent()) {
            mailService.sendResetPasswordEmail(user.get(), locale);
        } else {
            log.debug("Cannot request password for email='{}', user does not exist", email);
        }
    }

    @Transactional
    public void resetPassword(final String token, final String newPassword) {
        final Jws<Claims> claims = Optional.ofNullable(token)
                .map(jwtTokenService::getClaims)
                .orElseThrow(() -> new RuntimeException("Reset token is empty"));
        if (!TokenUtils.isPasswordResetToken(claims.getBody())) {
            throw new GraphQLException("Not a reset token");
        }

        final Long userId = TokenUtils.getUserId(claims.getBody());
        final User user = findById(userId);

        user.setPassword(passwordEncoder.encode(newPassword));

        if (TokenUtils.isUserActivationToken(claims.getBody())) {
            user.setEnabled(true);
        }

        userRepository.save(user);
    }

    public User findLoggedInUser() {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = principal.getId();
        return userRepository.getOne(id);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public List<User> findUsers() {
        return userRepository.findAll();
    }

    public Boolean isEmailRegistered(String email) {
        return isEmailRegistered(email, null);
    }

    public Boolean isEmailRegistered(String email, Long exludeId) {
        return exludeId == null
                ? userRepository.existsByEmail(email)
                : userRepository.existsByEmailAndIdIsNot(email, exludeId);
    }

    public Boolean isUsernameRegistered(String username) {
        return isUsernameRegistered(username, null);
    }

    public Boolean isUsernameRegistered(String username, Long excludeId) {
        return excludeId == null
                ? userRepository.existsByUsername(username)
                : userRepository.existsByUsernameAndIdIsNot(username, excludeId);
    }

    public User removeUser(Long id) {
        UserPrincipal principal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long currentUserId = principal.getId();

        if (id.equals(currentUserId)) {
            throw new CurrentUserDeleteException(id);
        }

        User user = findById(id);
        userRepository.delete(user);
        return user;
    }
}
