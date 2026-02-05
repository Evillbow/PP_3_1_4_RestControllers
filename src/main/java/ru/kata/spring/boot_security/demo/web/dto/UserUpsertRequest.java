package ru.kata.spring.boot_security.demo.web.dto;

import javax.validation.constraints.*;


import java.util.Set;

public class UserUpsertRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must be <= 50 chars")
    private String name;

    @Size(max = 50, message = "Surname must be <= 50 chars")
    private String surname;

    @Min(value = 0, message = "Year must be >= 0")
    @Max(value = 150, message = "Year must be <= 150")
    private Integer year;

    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email")
    @Size(max = 100, message = "Username must be <= 100 chars")
    private String username;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Password is required")
    @Size(min = 6, max = 72, message = "Password must be 6..72 chars")
    private String password;

    @NotEmpty(message = "At least one role is required")
    private Set<String> roles;

    // getters/setters

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }

    @Min(0) @Max(150)
    public Integer getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
}

