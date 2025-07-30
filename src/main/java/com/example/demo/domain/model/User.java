package com.example.demo.domain.model;

import java.util.UUID;

public class User {
    private UUID id;
    private String name;
    private String email;
    private String pass;
    private Role role;

    public User(UUID id, String name, String email, String pass) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.role = Role.USER; // Por defecto es USER
    }

    public User(UUID id, String name, String email, String pass, Role role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.pass = pass;
        this.role = role;
    }

    // Getters y setters
    public UUID getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getEmail() {
        return email;
    }
    public String getPass() {
        return pass;
    }
    public Role getRole() {
        return role;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }
    
    public void setRole(Role role) {
        this.role = role;
    }
}
