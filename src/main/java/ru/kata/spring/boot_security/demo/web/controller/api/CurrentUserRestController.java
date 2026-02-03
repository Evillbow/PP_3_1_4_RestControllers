package ru.kata.spring.boot_security.demo.web.controller.api;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kata.spring.boot_security.demo.web.dto.UserDto;
import ru.kata.spring.boot_security.demo.web.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.web.model.User;

@RestController
@RequestMapping("/api/user")
public class CurrentUserRestController {

    @GetMapping("/me")
    public UserDto me(@AuthenticationPrincipal User user) {
        return UserMapper.toDto(user);
    }
}
