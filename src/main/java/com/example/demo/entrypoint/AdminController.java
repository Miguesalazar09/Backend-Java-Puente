package com.example.demo.entrypoint;

import com.example.demo.application.usecase.UserService;
import com.example.demo.domain.model.User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/users/{id}")
    public User getUserById(@PathVariable UUID id) {
        return userService.getUserById(id);
    }
    
    @PostMapping("/users")
    public CreateUserResponse createUser(@RequestBody AdminUserDTO dto) {
        try {
            User newUser = userService.register(dto.name(), dto.email(), dto.password(), dto.role());
            return new CreateUserResponse(true, "Usuario creado exitosamente", newUser);
        } catch (IllegalArgumentException e) {
            return new CreateUserResponse(false, "Error: " + e.getMessage(), null);
        } catch (RuntimeException e) {
            return new CreateUserResponse(false, "Error: " + e.getMessage(), null);
        }
    }

    @PutMapping("/users/{id}")
    public UpdateResponse updateUser(@PathVariable UUID id, @RequestBody UpdateUserDTO dto) {
        try {
            User updatedUser = userService.updateUser(id, dto.name(), dto.email(), dto.password(), dto.role());
            return new UpdateResponse(true, "Usuario actualizado exitosamente", updatedUser);
        } catch (IllegalArgumentException e) {
            return new UpdateResponse(false, "Error: " + e.getMessage(), null);
        } catch (RuntimeException e) {
            // Si es un error de "not found", propagar la excepción para que GlobalExceptionHandler devuelva 404
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("not found")) {
                throw e; // Propagar la excepción
            }
            return new UpdateResponse(false, "Error: " + e.getMessage(), null);
        }
    }

    @DeleteMapping("/users/{id}")
    public DeleteResponse deleteUser(@PathVariable UUID id) {
        try {
            User user = userService.getUserById(id);
            userService.deleteUser(id);
            return new DeleteResponse(true, "Usuario '" + user.getName() + "' eliminado exitosamente", id);
        } catch (IllegalArgumentException e) {
            return new DeleteResponse(false, "Error: " + e.getMessage(), id);
        } catch (IllegalStateException e) {
            return new DeleteResponse(false, "Error: " + e.getMessage(), id);
        }
    }
    

    record AdminUserDTO(String name, String email, String password, com.example.demo.domain.model.Role role) {}
    record UpdateUserDTO(String name, String email, String password, com.example.demo.domain.model.Role role) {}
    record DeleteResponse(boolean success, String message, UUID deletedId) {}
    record UpdateResponse(boolean success, String message, User user) {}
    record CreateUserResponse(boolean success, String message, User user) {}
}
