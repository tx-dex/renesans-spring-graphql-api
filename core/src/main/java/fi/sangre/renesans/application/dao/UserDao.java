package fi.sangre.renesans.application.dao;

import fi.sangre.renesans.exception.UserNotFoundException;
import fi.sangre.renesans.persistence.model.Role;
import fi.sangre.renesans.persistence.model.User;
import fi.sangre.renesans.persistence.repository.RoleRepository;
import fi.sangre.renesans.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@RequiredArgsConstructor
@Slf4j

@Component
public class UserDao {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    //TODO: create intermediate type
    @NonNull
    @Transactional(readOnly = true)
    public User getByIdOrThrow(@NonNull final Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    //TODO: create intermediate type
    @NonNull
    @Transactional(readOnly = true)
    public Collection<User> getUsers() {
        return userRepository.findAll();
    }

    @NonNull
    @Transactional(readOnly = true)
    public Collection<Role> getRoles() {
        return roleRepository.findAll();
    }
}
