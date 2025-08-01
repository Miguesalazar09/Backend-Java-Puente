package com.example.userservice.entrypoint;

import com.example.userservice.application.usecase.AuthService;
import com.example.userservice.application.usecase.UserService;
import com.example.userservice.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            User user = userService.register(request.username, request.email, request.password);
            return ResponseEntity.ok(new RegisterResponse(true, "Usuario registrado exitosamente", user.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new RegisterResponse(false, e.getMessage(), null));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthService.AuthRequest request
    ) {
        try {
            AuthService.AuthResponse response = authService.authenticate(request);
            return ResponseEntity.ok(new TokenResponse(response.getToken()));
        } catch (Exception e) {
            e.printStackTrace(); // Para ver el error en los logs
            return ResponseEntity.status(500).body(new LoginErrorResponse(false, "Login failed: " + e.getMessage()));
        }
    }

    public static class RegisterRequest {
        public String username;
        public String email;
        public String password;
    }

    public static class RegisterResponse {
        public boolean success;
        public String message;
        public java.util.UUID userId;

        public RegisterResponse(boolean success, String message, java.util.UUID userId) {
            this.success = success;
            this.message = message;
            this.userId = userId;
        }
    }

    public static class LoginErrorResponse {
        public boolean success;
        public String message;

        public LoginErrorResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    public static class TokenResponse {
        public String token;

        public TokenResponse(String token) {
            this.token = token;
        }
    }
}
