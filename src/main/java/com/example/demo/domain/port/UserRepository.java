package com.example.demo.domain.port;

import com.example.demo.domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    User save(User user);
    List<User> findAll();
    Optional<User> findByEmail(String email);
    Optional<User> findById(UUID id);
    void deleteById(UUID id);
    boolean existsByEmail(String email);
}