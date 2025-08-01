package com.example.userservice.entrypoint;

import com.example.userservice.application.usecase.UserService;
import com.example.userservice.domain.model.User;
import com.example.userservice.domain.model.Role;
import com.example.userservice.domain.port.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AdminController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserDTO dto, Authentication authentication) {
        // Validaciones de campos obligatorios
        if (dto.name() == null || dto.name().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Error: El nombre es obligatorio"));
        }
        
        if (dto.email() == null || dto.email().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Error: El email es obligatorio"));
        }
        
        if (dto.password() == null || dto.password().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Error: La contraseña es obligatoria"));
        }

        // Role por defecto USER si no se especifica
        Role userRole = dto.role() != null ? dto.role() : Role.USER;

        // Validar que el email no exista
        if (userRepository.existsByEmail(dto.email().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, "Error: Ya existe un usuario con el email: " + dto.email().trim()));
        }
        
        try {
            User newUser = userService.register(dto.name().trim(), dto.email().trim(), dto.password(), userRole);
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateUserResponse(true, "Usuario creado exitosamente por administrador", newUser));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "Error interno: " + e.getMessage()));
        }
    }

    @GetMapping("/users")
    public ResponseEntity<ListUsersResponse> getAllUsers() {
        try {
            List<User> users = userService.findAll();
            return ResponseEntity.ok(new ListUsersResponse(true, "Usuarios obtenidos exitosamente", users));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ListUsersResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        try {
            User user = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            return ResponseEntity.ok(new UserResponse(true, "Usuario encontrado", user));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UserResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<DeleteUserResponse> deleteUser(@PathVariable UUID id, Authentication authentication) {
        try {
            // Verificar que el usuario existe
            User userToDelete = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Evitar que el admin se elimine a sí mismo
            String currentUserEmail = authentication.getName();
            if (userToDelete.getEmail().equals(currentUserEmail)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new DeleteUserResponse(false, "Error: No puedes eliminar tu propio usuario"));
            }
            
            userService.deleteUser(id);
            return ResponseEntity.ok(new DeleteUserResponse(true, "Usuario eliminado exitosamente"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DeleteUserResponse(false, "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UpdateUserResponse> updateUser(@PathVariable UUID id, @RequestBody UpdateUserDTO dto, Authentication authentication) {
        try {
            // Verificar que el usuario existe
            User userToUpdate = userService.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            
            // Validaciones de campos si se envían
            if (dto.name() != null && dto.name().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: El nombre no puede estar vacío", null));
            }
            
            if (dto.email() != null && dto.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: El email no puede estar vacío", null));
            }
            
            if (dto.password() != null && dto.password().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: La contraseña no puede estar vacía", null));
            }
            
            // Verificar si el email ya existe (si se está cambiando)
            if (dto.email() != null && !dto.email().trim().equals(userToUpdate.getEmail())) {
                if (userRepository.existsByEmail(dto.email().trim())) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new UpdateUserResponse(false, "Error: Ya existe un usuario con el email: " + dto.email().trim(), null));
                }
            }
            
            // Evitar que el admin se quite su propio rol ADMIN
            String currentUserEmail = authentication.getName();
            if (userToUpdate.getEmail().equals(currentUserEmail) && dto.role() != null && dto.role() != Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: No puedes cambiar tu propio rol de ADMIN", null));
            }
            
            // Usar los valores actuales si no se envían nuevos valores
            String newName = dto.name() != null ? dto.name().trim() : userToUpdate.getName();
            String newEmail = dto.email() != null ? dto.email().trim() : userToUpdate.getEmail();
            String newPassword = dto.password() != null ? dto.password() : null; // null significa no cambiar
            Role newRole = dto.role() != null ? dto.role() : userToUpdate.getRole();
            
            User updatedUser = userService.updateUser(id, newName, newEmail, newPassword, newRole);
            return ResponseEntity.ok(new UpdateUserResponse(true, "Usuario actualizado exitosamente", updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new UpdateUserResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    // DTOs
    record CreateUserDTO(String name, String email, String password, Role role) {}
    record UpdateUserDTO(String name, String email, String password, Role role) {}
    record ChangeRoleDTO(Role role) {}
    
    record CreateUserResponse(boolean success, String message, User user) {}
    record ListUsersResponse(boolean success, String message, List<User> users) {}
    record UserResponse(boolean success, String message, User user) {}
    record UpdateUserResponse(boolean success, String message, User user) {}
    record DeleteUserResponse(boolean success, String message) {}
    record ChangeRoleResponse(boolean success, String message) {}
    record ErrorResponse(boolean success, String message) {}
}
