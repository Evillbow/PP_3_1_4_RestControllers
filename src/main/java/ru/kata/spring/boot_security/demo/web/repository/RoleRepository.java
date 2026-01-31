package ru.kata.spring.boot_security.demo.web.repository;

import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.web.model.Role;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class RoleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<Role> findByName(String name) {
        return entityManager.createQuery(
                        "select r from Role r where r.name = :name", Role.class)
                .setParameter("name", name)
                .getResultStream()
                .findFirst();
    }

    public void save(Role role) {
        entityManager.persist(role);
    }
}

