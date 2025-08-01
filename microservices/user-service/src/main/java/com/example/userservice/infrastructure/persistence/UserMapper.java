package com.example.userservice.infrastructure.persistence;

import com.example.userservice.domain.model.Role;
import com.example.userservice.domain.model.User;

public class UserMapper {

    public static User toDomain(UserEntity entity) {
        if (entity == null) return null;
        return new User(entity.getId(), entity.getName(), entity.getEmail(), entity.getPassword(), entity.getRole());
    }

    public static UserEntity toEntity(User user) {
        if (user == null) return null;
        if (user.getId() == null) {
            // Para nuevos usuarios sin ID, usar constructor sin ID para que Hibernate genere el UUID
            return new UserEntity(user.getName(), user.getEmail(), user.getPass(), user.getRole());
        }
        return new UserEntity(user.getId(), user.getName(), user.getEmail(), user.getPass(), user.getRole());
    }

    public static UserEntity toEntity(String name, String email, String password, Role role) {
        return new UserEntity(name, email, password, role);
    }
}
