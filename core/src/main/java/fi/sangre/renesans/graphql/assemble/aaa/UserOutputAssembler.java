package fi.sangre.renesans.graphql.assemble.aaa;

import com.google.common.collect.ImmutableSet;
import fi.sangre.renesans.aaa.UserPrincipal;
import fi.sangre.renesans.graphql.output.aaa.UserOutput;
import fi.sangre.renesans.graphql.output.aaa.UserRoleOutput;
import fi.sangre.renesans.persistence.model.User;
import fi.sangre.renesans.persistence.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;

import static java.util.stream.Collectors.*;

@RequiredArgsConstructor
@Slf4j

@Component
public class UserOutputAssembler {
    private final RoleRepository roleRepository;

    @NonNull
    public Collection<UserOutput> from(@NonNull final Collection<User> users) {
        return users.stream()
                .map(this::from)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
    }

    @NonNull
    public UserOutput from(@NonNull final User entity) {
        return UserOutput.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .username(entity.getUsername())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .enabled(entity.isEnabled())
                .roles(entity.getRoles().stream()
                .map(e -> UserRoleOutput.builder()
                        .name(e.getName())
                        .title(e.getTitle())
                        .build())
                        .collect(collectingAndThen(toSet(), Collections::unmodifiableSet)))
                .build();
    }

    @NonNull
    @Transactional(readOnly = true)
    public UserOutput from(@NonNull final UserPrincipal principal) {
        return UserOutput.builder()
                .id(principal.getId())
                .email(principal.getEmail())
                .username(principal.getUsername())
                .firstName(principal.getFirstName())
                .lastName(principal.getLastName())
                .enabled(principal.isEnabled())
                .roles(roleRepository.findByUsersIdIn(ImmutableSet.of(principal.getId())).stream()
                        .map(e -> UserRoleOutput.builder()
                                .name(e.getName())
                                .title(e.getTitle())
                                .build())
                        .collect(collectingAndThen(toSet(), Collections::unmodifiableSet)))
                .build();
    }
}
