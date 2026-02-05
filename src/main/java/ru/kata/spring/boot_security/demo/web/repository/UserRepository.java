package ru.kata.spring.boot_security.demo.web.repository;

import org.springframework.stereotype.Repository;
import ru.kata.spring.boot_security.demo.web.model.User;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<User> findAll() {
        return entityManager.createQuery("select u from User u", User.class)
                .getResultList();
    }

    public Optional<User> findByUsernameWithRoles(String username) {
        return entityManager.createQuery(
                        "select distinct u from User u " +
                                "left join fetch u.roles " +
                                "where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    public User save(User user) {
        entityManager.persist(user);
        entityManager.flush(); // важно для GenerationType.IDENTITY, чтобы id появился сразу
        return user;
    }

    public User update(User user) {
        return entityManager.merge(user);
    }

    public void delete(Long id) {
        User user = findById(id);
        if (user != null) {
            entityManager.remove(user);
        }
    }

    public Optional<User> findByUsername(String username) {
        return entityManager.createQuery(
                        "select u from User u where u.username = :username", User.class)
                .setParameter("username", username)
                .getResultStream()
                .findFirst();
    }

    public boolean existsByUsername(String username) {
        Long cnt = entityManager.createQuery(
                        "select count(u) from User u where u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }

    public List<User> findAllWithRoles() {
        var graph = entityManager.getEntityGraph("User.roles");
        return entityManager.createQuery("select u from User u", User.class)
                .setHint("javax.persistence.fetchgraph", graph)
                .getResultList();
    }

    public User findByIdWithRoles(Long id) {
        return entityManager.createQuery(
                        "select distinct u from User u left join fetch u.roles where u.id = :id", User.class)
                .setParameter("id", id)
                .getSingleResult();
    }

    public boolean existsByUsernameAndNotId(String username, Long id) {
        Long cnt = entityManager.createQuery(
                        "select count(u) from User u where u.username = :username and u.id <> :id", Long.class)
                .setParameter("username", username)
                .setParameter("id", id)
                .getSingleResult();
        return cnt != null && cnt > 0;
    }


}


