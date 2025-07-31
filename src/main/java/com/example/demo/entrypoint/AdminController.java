package com.example.demo.entrypoint;

import com.example.demo.application.usecase.UserService;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;
    private final UserRepository userRepository;

    public AdminController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable String id) {
        try {
            // Validar formato UUID
            UUID uuid;
            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(false, "Error: El ID debe tener formato UUID válido (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)"));
            }
            
            User user = userService.getUserById(uuid);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(false, "Error: Usuario no encontrado"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "Error interno: " + e.getMessage()));
        }
    }
    
    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody AdminUserDTO dto) {
        // Validaciones con códigos HTTP correctos
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
        
        if (dto.role() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Error: El rol es obligatorio"));
        }

        // Validar que el email no exista antes de intentar crear el usuario
        if (userRepository.existsByEmail(dto.email().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, "Error: Ya existe un usuario con el email: " + dto.email().trim()));
        }

        try {
            User newUser = userService.register(dto.name().trim(), dto.email().trim(), dto.password(), dto.role());
            return ResponseEntity.status(HttpStatus.CREATED).body(new CreateUserResponse(true, "Usuario creado exitosamente", newUser));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse(false, "Error: " + e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(false, "Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, "Error interno: " + e.getMessage()));
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody UpdateUserDTO dto) {
        try {
            // Validar formato UUID
            UUID uuid;
            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: El ID debe tener formato UUID válido (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)"));
            }
            
            // Validaciones con códigos HTTP correctos
            if (dto.name() != null && dto.name().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: El nombre no puede estar vacío"));
            }
            
            if (dto.email() != null && dto.email().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: El email no puede estar vacío"));
            }
            
            if (dto.password() != null && dto.password().trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new UpdateUserResponse(false, "Error: La contraseña no puede estar vacía"));
            }

            userService.updateUser(uuid, dto.name(), dto.email(), dto.password(), dto.role());
            return ResponseEntity.ok(new UpdateUserResponse(true, "Usuario actualizado exitosamente"));
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new UpdateUserResponse(false, "Error: " + e.getMessage()));
            }
            if (e.getMessage().contains("ya existe")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new UpdateUserResponse(false, "Error: " + e.getMessage()));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new UpdateUserResponse(false, "Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new UpdateUserResponse(false, "Error interno: " + e.getMessage()));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String id) {
        try {
            // Validar formato UUID
            UUID uuid;
            try {
                uuid = UUID.fromString(id);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new DeleteUserResponse(false, "Error: El ID debe tener formato UUID válido (xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx)"));
            }
            
            userService.deleteUser(uuid);
            return ResponseEntity.ok(new DeleteUserResponse(true, "Usuario eliminado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new DeleteUserResponse(false, "Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new DeleteUserResponse(false, "Error interno: " + e.getMessage()));
        }
    }
    

    record AdminUserDTO(String name, String email, String password, com.example.demo.domain.model.Role role) {}
    record UpdateUserDTO(String name, String email, String password, com.example.demo.domain.model.Role role) {}
    record DeleteResponse(boolean success, String message, UUID deletedId) {}
    record UpdateResponse(boolean success, String message, User user) {}
    record CreateUserResponse(boolean success, String message, User user) {}
    record ErrorResponse(boolean success, String message) {}
    record UpdateUserResponse(boolean success, String message) {}
    record DeleteUserResponse(boolean success, String message) {}
}
