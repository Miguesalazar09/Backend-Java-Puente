package com.example.demo.entrypoint;

import com.example.demo.application.usecase.UserService;
import com.example.demo.application.usecase.UserService.ProfileUpdateResult;
import com.example.demo.domain.model.User;
import com.example.demo.domain.port.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<?> register(@RequestBody UserDTO dto) {
        // Verificar si se intentó enviar un rol
        if (dto.role() != null) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(false, "Error: No se permite especificar el rol en el registro de usuarios. Los usuarios se registran automáticamente con rol USER."));
        }
        
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

        // Validar que el email no exista antes de intentar crear el usuario
        if (userRepository.existsByEmail(dto.email().trim())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(false, "Error: Ya existe un usuario con el email: " + dto.email().trim()));
        }
        
        try {
            User newUser = userService.register(dto.name().trim(), dto.email().trim(), dto.password());
            return ResponseEntity.status(HttpStatus.CREATED).body(newUser);
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

    @PostMapping("/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody UserDTO dto) {
        try {
            User newUser = userService.register(dto.name(), dto.email(), dto.password(), 
                com.example.demo.domain.model.Role.ADMIN);
            return ResponseEntity.ok(newUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(false, "Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(false, "Error: " + e.getMessage()));
        }
    }

    @PutMapping("/profile") //Define un endpoint HTTP tipo PUT en la ruta /profile
    public ResponseEntity<UpdateProfileResponse> updateProfile(@RequestBody UpdateProfileDTO dto, Authentication authentication) {
        try {
            // Obtener el email del usuario autenticado
            String currentUserEmail = authentication.getName();
            
            ProfileUpdateResult result = userService.updateUserProfile(
                currentUserEmail, 
                dto.name(), 
                dto.email(), 
                dto.password(),
                dto.role()
            );
            
            UpdateProfileResponse response = new UpdateProfileResponse(
                result.hasChanges(), 
                result.message()
            );
            
            // Si se intentó cambiar rol, devolver 400 Bad Request
            if (result.attemptedRoleChange()) {
                response = new UpdateProfileResponse(false, result.message());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Si no hay cambios, devolver 400 Bad Request
            if (!result.hasChanges()) {
                response = new UpdateProfileResponse(false, result.message());
                return ResponseEntity.badRequest().body(response);
            }
            
            // Si hubo cambios válidos, devolver 200 OK
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new UpdateProfileResponse(false, "Error: " + e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new UpdateProfileResponse(false, "Error: " + e.getMessage()));
        }
    }

    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication authentication) {
        try {
            // Obtener el email del usuario autenticado
            String currentUserEmail = authentication.getName();
            
            User user = userService.getUserByEmail(currentUserEmail);
            
            return ResponseEntity.ok(new ProfileResponse(true, "Perfil obtenido exitosamente", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                .body(new ProfileResponse(false, "Error: " + e.getMessage(), null));
        }
    }

    // SECCION DE DTOS
    record UserDTO(String name, String email, String password, com.example.demo.domain.model.Role role) {}
    record UpdateProfileDTO(String name, String email, String password, com.example.demo.domain.model.Role role) {}
    record UpdateProfileResponse(boolean success, String message) {}
    record ProfileResponse(boolean success, String message, User user) {}
    record ErrorResponse(boolean success, String message) {}
}
