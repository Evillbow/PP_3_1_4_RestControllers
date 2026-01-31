package ru.kata.spring.boot_security.demo.configs;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.web.model.Role;
import ru.kata.spring.boot_security.demo.web.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Set;

@Component
public class DataInit implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;

    private final PasswordEncoder passwordEncoder;

    public DataInit(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        Role adminRole = getOrCreateRole("ROLE_ADMIN");
        Role userRole  = getOrCreateRole("ROLE_USER");

        if (findUserByUsername("admin") == null) {
            User admin = new User();
            admin.setName("Admin");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setRoles(Set.of(adminRole, userRole));
            em.persist(admin);
        }

        if (findUserByUsername("user") == null) {
            User user = new User();
            user.setName("User");
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("user"));
            user.setRoles(Set.of(userRole));
            em.persist(user);
        }
    }

    private Role getOrCreateRole(String roleName) {
        Role role = em.createQuery("select r from Role r where r.name = :n", Role.class)
                .setParameter("n", roleName)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (role == null) {
            role = new Role(roleName);
            em.persist(role);
        }
        return role;
    }

    private User findUserByUsername(String username) {
        return em.createQuery("select u from User u where u.username = :u", User.class)
                .setParameter("u", username)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }
}

