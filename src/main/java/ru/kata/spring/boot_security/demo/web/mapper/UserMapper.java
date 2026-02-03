package ru.kata.spring.boot_security.demo.web.mapper;

import ru.kata.spring.boot_security.demo.web.dto.UserDto;
import ru.kata.spring.boot_security.demo.web.model.Role;
import ru.kata.spring.boot_security.demo.web.model.User;

import java.util.Set;
import java.util.stream.Collectors;

public final class UserMapper {

    private UserMapper() {
    }

    public static UserDto toDto(User user) {
        Set<String> roleNames = user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getSurname(),
                user.getYear(),
                user.getUsername(),
                roleNames
        );
    }
}

