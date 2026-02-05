package ru.kata.spring.boot_security.demo.web.service;

public class DuplicateUsernameException extends RuntimeException {
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}

