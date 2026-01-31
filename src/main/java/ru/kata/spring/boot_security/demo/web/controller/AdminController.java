package ru.kata.spring.boot_security.demo.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.web.model.User;
import ru.kata.spring.boot_security.demo.web.service.UserService;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    // /admin -> список пользователей
    @GetMapping
    public String adminHome(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin"; // <-- ВАЖНО: теперь используем admin.html
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam("username") String username,
                          @RequestParam("password") String password,
                          @RequestParam("name") String name,
                          @RequestParam(value = "surname", required = false) String surname,
                          @RequestParam(value = "year", required = false) Integer year) {

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setName(name);
        user.setSurname(surname);
        user.setYear(year);

        userService.addUser(user);

        return "redirect:/admin";
    }


    @PostMapping("/users/delete")
    public String deleteUser(@RequestParam("id") Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit")
    public String editUser(@RequestParam("id") Long id, Model model) {
        model.addAttribute("user", userService.getUser(id));
        return "edit";
    }

    @PostMapping("/users/update")
    public String updateUser(@RequestParam("id") Long id,
                             @RequestParam("username") String username,
                             @RequestParam(value = "password", required = false) String password,
                             @RequestParam("name") String name,
                             @RequestParam(value = "surname", required = false) String surname,
                             @RequestParam(value = "year", required = false) Integer year) {

        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setPassword(password); // может быть пустым -> обработаем в сервисе
        user.setName(name);
        user.setSurname(surname);
        user.setYear(year);

        userService.updateUser(user);
        return "redirect:/admin/users";
    }
}



