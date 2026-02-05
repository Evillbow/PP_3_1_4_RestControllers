package ru.kata.spring.boot_security.demo.web.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.web.model.Role;
import ru.kata.spring.boot_security.demo.web.model.User;
import ru.kata.spring.boot_security.demo.web.repository.RoleRepository;
import ru.kata.spring.boot_security.demo.web.repository.UserRepository;

import java.util.List;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAllWithRoles();
    }

    @Transactional
    public User addUser(User user, Set<String> roleNames) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new DuplicateUsernameException(user.getUsername());
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(resolveRoles(roleNames));
        return userRepository.save(user);
    }



    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findByIdWithRoles(id);
    }

    @Transactional
    public void updateUser(User user, Set<String> roleNames) {

        User dbUser = userRepository.findById(user.getId());

        if (!dbUser.getUsername().equals(user.getUsername())
                && userRepository.existsByUsernameAndNotId(user.getUsername(), user.getId())) {
            throw new DuplicateUsernameException(user.getUsername());
        }

        dbUser.setUsername(user.getUsername());
        dbUser.setName(user.getName());
        dbUser.setSurname(user.getSurname());
        dbUser.setYear(user.getYear());
        dbUser.setRoles(resolveRoles(roleNames));

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.update(dbUser);
    }


    private Set<Role> resolveRoles(Set<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            roleNames = Set.of("ROLE_USER");
        }
        return roleNames.stream()
                .map(rn -> roleRepository.findByName(rn)
                        .orElseThrow(() -> new IllegalStateException(rn + " not found")))
                .collect(java.util.stream.Collectors.toSet());
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException(name + " not found"));
    }


}


