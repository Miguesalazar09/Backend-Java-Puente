package com.example.demo.infrastructure.entity;

import com.example.demo.domain.model.Role;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
public class UserEntity {
    @Id
    private UUID id;
    private String name;
    private String email;
    private String pass;
    
    @Enumerated(EnumType.STRING)
    private Role role;

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

    // Setters
    public void setId(UUID id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public void setPass(String pass) {
        this.pass = pass;
    }
    public void setRole(Role role) {
        this.role = role;
    }
}
