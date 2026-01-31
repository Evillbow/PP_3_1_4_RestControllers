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

    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Role roleUser = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER not found"));

        user.setRoles(Set.of(roleUser)); // всем новым по умолчанию ROLE_USER

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        userRepository.delete(id);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public void updateUser(User user) {
        User dbUser = userRepository.findById(user.getId());

        dbUser.setUsername(user.getUsername());
        dbUser.setName(user.getName());
        dbUser.setSurname(user.getSurname());
        dbUser.setYear(user.getYear());

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            dbUser.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        userRepository.update(dbUser);
    }
}


