package ru.kata.spring.boot_security.demo.web.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.web.dto.UserDto;
import ru.kata.spring.boot_security.demo.web.dto.UserUpsertRequest;
import ru.kata.spring.boot_security.demo.web.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.web.model.User;
import ru.kata.spring.boot_security.demo.web.service.UserService;
import org.springframework.validation.annotation.Validated;
import ru.kata.spring.boot_security.demo.web.dto.ValidationGroups;


import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
public class AdminRestController {

    private final UserService userService;

    public AdminRestController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> allUsers() {
        return userService.getAllUsers()
                .stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public UserDto one(@PathVariable Long id) {
        return UserMapper.toDto(userService.getUser(id));
    }

    @PostMapping
    public ResponseEntity<UserDto> create(@Validated(ValidationGroups.Create.class) @RequestBody UserUpsertRequest req) {
        User user = new User();
        user.setName(req.getName());
        user.setSurname(req.getSurname());
        user.setYear(req.getYear());
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword());

        User created = userService.addUser(user, req.getRoles());

        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(created));
    }


    @PutMapping("/{id}")
    public UserDto update(@PathVariable Long id,
                          @Validated(ValidationGroups.Update.class) @RequestBody UserUpsertRequest req) {
        User user = new User();
        user.setId(id);
        user.setName(req.getName());
        user.setSurname(req.getSurname());
        user.setYear(req.getYear());
        user.setUsername(req.getUsername());
        user.setPassword(req.getPassword()); // может быть пустым

        userService.updateUser(user, req.getRoles());
        return UserMapper.toDto(userService.getUser(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}

