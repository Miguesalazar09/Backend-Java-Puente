package com.example.demo.application.usecase;

import com.example.demo.domain.model.Role;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.UserRepository;
import com.example.demo.infrastructure.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;

    public UserService(UserRepository userRepository, CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.userDetailsService = userDetailsService;
    }

    public User register(String name, String email, String password) {
        return register(name, email, password, Role.USER);
    }

    public User register(String name, String email, String password, Role role) {
        // Validaciones
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (role == null) {
            throw new IllegalArgumentException("Role cannot be null");
        }
        
        String hashedPassword = userDetailsService.encodePassword(password);
        return userRepository.save(new User(UUID.randomUUID(), name, email, hashedPassword, role));
    }

    public void deleteUser(UUID id) {
        // Verificar que el usuario existe antes de eliminarlo
        if (!userRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User updateUser(UUID id, String name, String email, String password, Role role) {
        // Verificar que el usuario existe
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Validaciones
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email != null && email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password != null && password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Crear usuario actualizado (solo actualizar campos no nulos)
        String updatedName = name != null ? name : existingUser.getName();
        String updatedEmail = email != null ? email : existingUser.getEmail();
        String updatedPassword = password != null ? userDetailsService.encodePassword(password) : existingUser.getPass();
        Role updatedRole = role != null ? role : existingUser.getRole();
        
        User updatedUser = new User(id, updatedName, updatedEmail, updatedPassword, updatedRole);
        return userRepository.save(updatedUser);
    }

    public ProfileUpdateResult updateUserProfile(String email, String name, String newEmail, String password, Role requestedRole) {
        // Buscar el usuario por email (el usuario autenticado)
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        // Validaciones
        if (name != null && name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (newEmail != null && newEmail.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (password != null && password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        
        // Verificar que el nuevo email no esté en uso por otro usuario
        if (newEmail != null && !newEmail.equals(existingUser.getEmail())) {
            userRepository.findByEmail(newEmail).ifPresent(user -> {
                throw new IllegalArgumentException("Email already in use by another user");
            });
        }
        
        // Detectar intento de cambio de rol
        boolean attemptedRoleChange = false;
        if (requestedRole != null && !requestedRole.equals(existingUser.getRole())) {
            attemptedRoleChange = true;
            logger.warn("Usuario {} intentó cambiar rol de {} a {} mediante actualización de perfil", 
                       existingUser.getEmail(), existingUser.getRole(), requestedRole);
        }
        
        // Crear valores actualizados (solo actualizar campos no nulos, mantener rol original)
        String updatedName = name != null ? name : existingUser.getName();
        String updatedEmail = newEmail != null ? newEmail : existingUser.getEmail();
        String updatedPassword = password != null ? userDetailsService.encodePassword(password) : existingUser.getPass();
        
        // Verificar si realmente hubo cambios
        boolean hasChanges = false;
        StringBuilder changeDetails = new StringBuilder();
        
        if (!updatedName.equals(existingUser.getName())) {
            hasChanges = true;
            changeDetails.append("nombre");
        }
        
        if (!updatedEmail.equals(existingUser.getEmail())) {
            hasChanges = true;
            if (changeDetails.length() > 0) changeDetails.append(", ");
            changeDetails.append("email");
        }
        
        if (password != null) {
            hasChanges = true;
            if (changeDetails.length() > 0) changeDetails.append(", ");
            changeDetails.append("contraseña");
        }
        
        // Construir mensaje según los cambios realizados y el intento de cambio de rol
        String message;
        if (attemptedRoleChange && !hasChanges) {
            message = "No se realizaron cambios en el perfil. Los cambios de rol no están permitidos en este endpoint";
        } else if (attemptedRoleChange && hasChanges) {
            message = "Perfil actualizado exitosamente (" + changeDetails.toString() + 
                     "). Nota: Los cambios de rol no están permitidos en este endpoint";
        } else if (!hasChanges) {
            message = "No se realizaron cambios en el perfil";
        } else {
            message = "Perfil actualizado exitosamente (" + changeDetails.toString() + ")";
        }
        
        User updatedUser = new User(existingUser.getId(), updatedName, updatedEmail, updatedPassword, existingUser.getRole());
        User savedUser = userRepository.save(updatedUser);
        
        return new ProfileUpdateResult(savedUser, message, hasChanges, attemptedRoleChange);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public record ProfileUpdateResult(User user, String message, boolean hasChanges, boolean attemptedRoleChange) {}
}
