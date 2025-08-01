package com.example.userservice.application.usecase;

import com.example.userservice.domain.model.Role;
import com.example.userservice.domain.model.User;
import com.example.userservice.domain.port.UserRepository;
import com.example.userservice.infrastructure.security.CustomUserDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
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
        
        // Verificar email único
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        String hashedPassword = userDetailsService.encodePassword(password);
        return userRepository.save(new User(null, name, email, hashedPassword, role));
    }

    public void deleteUser(UUID id) {
        if (!userRepository.findById(id).isPresent()) {
            throw new IllegalArgumentException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public User updateUser(UUID id, String name, String email, String password, Role role) {
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
        
        // Verificar email único si se está cambiando
        if (email != null && !email.equals(existingUser.getEmail()) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }
        
        String updatedName = name != null ? name : existingUser.getName();
        String updatedEmail = email != null ? email : existingUser.getEmail();
        String updatedPassword = password != null ? userDetailsService.encodePassword(password) : existingUser.getPass();
        Role updatedRole = role != null ? role : existingUser.getRole();
        
        User updatedUser = new User(id, updatedName, updatedEmail, updatedPassword, updatedRole);
        return userRepository.save(updatedUser);
    }

    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User updateProfile(String email, String name, String newEmail, String password) {
        User existingUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
        
        if (newEmail != null && !newEmail.equals(existingUser.getEmail()) && userRepository.existsByEmail(newEmail)) {
            throw new IllegalArgumentException("Email already in use by another user");
        }
        
        String updatedName = name != null ? name : existingUser.getName();
        String updatedEmail = newEmail != null ? newEmail : existingUser.getEmail();
        String updatedPassword = password != null ? userDetailsService.encodePassword(password) : existingUser.getPass();
        
        User updatedUser = new User(existingUser.getId(), updatedName, updatedEmail, updatedPassword, existingUser.getRole());
        return userRepository.save(updatedUser);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public ProfileUpdateResult updateUserProfile(String currentEmail, String name, String newEmail, String password, Role role) {
        boolean hasChanges = false;
        boolean attemptedRoleChange = false;
        String message = "";

        try {
            User existingUser = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Verificar si se intentó cambiar el rol
            if (role != null && !role.equals(existingUser.getRole())) {
                attemptedRoleChange = true;
                message = "No se permite cambiar el rol del usuario";
                return new ProfileUpdateResult(false, attemptedRoleChange, message);
            }

            // Verificar qué campos han cambiado
            boolean nameChanged = name != null && !name.trim().isEmpty() && !name.equals(existingUser.getName());
            boolean emailChanged = newEmail != null && !newEmail.trim().isEmpty() && !newEmail.equals(existingUser.getEmail());
            boolean passwordChanged = password != null && !password.trim().isEmpty();

            if (!nameChanged && !emailChanged && !passwordChanged) {
                message = "No se han detectado cambios en el perfil";
                return new ProfileUpdateResult(false, false, message);
            }

            // Validar email único si se está cambiando
            if (emailChanged && userRepository.existsByEmail(newEmail)) {
                throw new IllegalArgumentException("Ya existe un usuario con el email: " + newEmail);
            }

            // Realizar los cambios
            String updatedName = nameChanged ? name.trim() : existingUser.getName();
            String updatedEmail = emailChanged ? newEmail.trim() : existingUser.getEmail();
            String updatedPassword = passwordChanged ? userDetailsService.encodePassword(password) : existingUser.getPass();

            User updatedUser = new User(existingUser.getId(), updatedName, updatedEmail, updatedPassword, existingUser.getRole());
            userRepository.save(updatedUser);

            hasChanges = true;
            message = "Perfil actualizado exitosamente";
            return new ProfileUpdateResult(hasChanges, false, message);

        } catch (Exception e) {
            message = e.getMessage();
            return new ProfileUpdateResult(false, false, message);
        }
    }

    public record ProfileUpdateResult(boolean hasChanges, boolean attemptedRoleChange, String message) {}
}
