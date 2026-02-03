package ru.kata.spring.boot_security.demo.web.dto;

import java.util.Set;

public class UserDto {
    private Long id;
    private String name;
    private String surname;
    private Integer year;
    private String username;
    private Set<String> roles;

    public UserDto() {
    }

    public UserDto(Long id, String name, String surname, Integer year, String username, Set<String> roles) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.year = year;
        this.username = username;
        this.roles = roles;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}

